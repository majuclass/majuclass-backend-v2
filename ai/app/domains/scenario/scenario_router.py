from fastapi import APIRouter, HTTPException, status
from app.common.api_response import ApiResponse

# Pydantic 스키마 (요청/응답)
from app.domains.scenario.models.scenario_models import (
    GenerateScenarioRequest,      # prompt, question_count, options_per_question, categoryId, (선택)image sizes
    GenerateScenarioResponse,     # scenario + uploadSummary
    AutoCreateScenarioRequest,
    AutoCreateScenarioResponse,
    SequenceData,
    OptionData,
)

# 서비스 오케스트레이션
from app.domains.scenario.services.scenario_service import ScenarioService

router = APIRouter(
    prefix="/ai",
    tags=["Scenario"],
)

scenario_service = ScenarioService()


@router.post(
    "/scenario/generate",
    response_model=ApiResponse[GenerateScenarioResponse],
    summary="시나리오 생성 (텍스트 + 이미지 업로드용 presigned + 업로드까지)",
    description=(
        "GMS(gpt-4.1)로 제목/요약/문항/보기(아주 짧은 텍스트)를 생성하고, "
        "DALL·E 3로 썸네일/배경/옵션 이미지를 생성하여 presigned URL(putObject)로 S3에 업로드합니다. "
        "응답에는 텍스트와 이미지 S3Key가 모두 포함됩니다. 저장은 프론트에서 Spring Boot로 요청하세요."
    ),
    status_code=status.HTTP_200_OK,
)
async def generate_scenario(
    request: GenerateScenarioRequest,
) -> ApiResponse[GenerateScenarioResponse]:
    try:
        # ===== 로그: 요청 수신 =====
        print("\n" + "=" * 60)
        print("[API] 시나리오 생성 요청 수신")
        print("=" * 60)
        print(f"prompt: {request.prompt[:80]}...")
        print(f"questions: {request.question_count}, options: {request.options_per_question}")
        print(f"categoryId: {request.categoryId}")
        if getattr(request, "image_size", None):
            print(f"image_size: {request.image_size}")
        if getattr(request, "option_image_size", None):
            print(f"option_image_size: {request.option_image_size}")

        # ===== 오케스트레이션 호출 =====
        # 내부에서 수행되는 일:
        # 1) gpt-4.1로 텍스트(짧은 optionText) 생성 + 검증
        # 2) 썸네일/배경/옵션별 S3 Key 생성 + presigned(putObject) 발급
        # 3) DALL·E 3로 이미지 bytes 생성 후 presigned PUT 업로드
        # 4) scenario JSON에 thumbnailS3Key/backgroundS3Key/optionS3Key 주입
        scenario, upload_summary = await scenario_service.generate_scenario_with_presigned(
            prompt=request.prompt,
            question_count=request.question_count,
            options_per_question=request.options_per_question,
            category_id=request.categoryId,
            image_size=getattr(request, "image_size", "1024x1024"),
            option_image_size=getattr(request, "option_image_size", "512x512"),
        )

        # ===== 응답 스키마 포장 =====
        response_data = GenerateScenarioResponse(
            scenario=scenario,          # 텍스트 + 이미지 S3Key 모두 포함 (저장은 Spring에서)
            uploadSummary=upload_summary  # { uploaded: [ref...], failedRefs: [{ref, reason}...] }
        )

        # ===== 로그: 응답 직전 =====
        print("\n" + "=" * 60)
        print("[API] 시나리오 생성 응답 전송")
        print("=" * 60)
        print(f"title: {response_data.scenario['title']}")
        print(f"thumbnailS3Key: {response_data.scenario['thumbnailS3Key']}")
        print(f"backgroundS3Key: {response_data.scenario['backgroundS3Key']}")
        print(f"total_sequences: {response_data.scenario['total_sequences']}")
        if response_data.uploadSummary.get("failedRefs"):
            print(f"이미지 실패 개수: {len(response_data.uploadSummary['failedRefs'])}")

        return ApiResponse.success(
            message="시나리오(텍스트+이미지) 생성이 완료되었습니다",
            data=response_data,
        )

    except ValueError as e:
        # 요청/LLM 검증 오류 → 400
        print(f"\n[API] 잘못된 요청/검증 실패: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"잘못된 요청입니다: {str(e)}",
        )
    except Exception as e:
        # 시스템/외부 API 오류 → 500
        print(f"\n[API] 서버 오류: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"시나리오 생성 중 오류가 발생했습니다: {str(e)}",
        )


@router.post(
    "/auto-create",
    response_model=ApiResponse[AutoCreateScenarioResponse],
    summary="시나리오 자동 생성 (DB 저장 없이 반환만)",
    description=(
        "LLM을 활용하여 시나리오를 자동 생성하고 이미지를 생성하여 S3에 업로드합니다. "
        "DB에 저장하지 않고 생성된 데이터만 반환합니다."
    ),
    status_code=status.HTTP_200_OK,
)
async def auto_create_scenario(
    request: AutoCreateScenarioRequest,
    use_rag: bool = True,  # 쿼리 파라미터로 RAG 사용 여부 제어 (기본값 True)
) -> ApiResponse[AutoCreateScenarioResponse]:
    try:
        print("\n" + "=" * 60)
        print("[API] 시나리오 자동 생성 요청 수신")
        print("=" * 60)
        print(f"prompt: {request.prompt[:80]}...")
        print(f"seq_cnt: {request.seq_cnt}, option_cnt: {request.option_cnt}")
        print(f"category_id: {request.category_id}")
        print(f"use_rag: {use_rag}")

        scenario, upload_summary = await scenario_service.generate_scenario_with_presigned(
            prompt=request.prompt,
            question_count=request.seq_cnt,
            options_per_question=request.option_cnt,
            category_id=request.category_id,
            image_size="1024x1024",
            option_image_size="512x512",
            use_rag=use_rag,  # RAG 파라미터 전달
        )

        sequences = []
        for seq in scenario.get("sequences", []):
            options = [
                OptionData(
                    optionNo=opt["optionNo"],
                    optionText=opt["optionText"],
                    optionS3Key=opt["optionS3Key"],
                    isAnswer=opt["isAnswer"]
                )
                for opt in seq.get("options", [])
            ]
            sequences.append(
                SequenceData(
                    seqNo=seq["seqNo"],
                    question=seq["question"],
                    options=options
                )
            )

        response_data = AutoCreateScenarioResponse(
            title=scenario["title"],
            summary=scenario["summary"],
            categoryId=scenario["categoryId"],
            thumbnailS3Key=scenario["thumbnailS3Key"],
            backgroundS3Key=scenario["backgroundS3Key"],
            sequences=sequences
        )

        print("\n" + "=" * 60)
        print("[API] 시나리오 자동 생성 응답 전송")
        print("=" * 60)
        print(f"title: {response_data.title}")
        print(f"총 시퀀스: {len(response_data.sequences)}")

        return ApiResponse.success(
            message="시나리오 자동 생성에 성공하였습니다.",
            data=response_data,
        )

    except ValueError as e:
        print(f"\n[API] 잘못된 요청/검증 실패: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"잘못된 요청입니다: {str(e)}",
        )
    except Exception as e:
        print(f"\n[API] 서버 오류: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"시나리오 자동 생성 중 오류가 발생했습니다: {str(e)}",
        )
