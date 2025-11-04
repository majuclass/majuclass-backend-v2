from fastapi import APIRouter, HTTPException, status
from app.domains.speech_to_text.models.stt_request_models import SpeechToTextRequestModel
from app.domains.speech_to_text.models.stt_response_models import SpeechToTextResponse
from app.domains.speech_to_text.services.speech_analysis_service import SpeechAnalysisService
from app.common.api_response import ApiResponse

router = APIRouter(
    prefix="/ai",
    tags=["STT"],
)
speech_analysis_service = SpeechAnalysisService()

@router.post(
    "/stt-analyze",
    response_model=ApiResponse[SpeechToTextResponse],
    summary="음성 텍스트 변환 후 정답 판별",
    description="S3의 오디오 파일을 STT로 변환한 후 정답과 비교하여 정답 여부를 반환합니다.",
    status_code=status.HTTP_200_OK
)
async def stt_analyze(
    request: SpeechToTextRequestModel
) -> ApiResponse[SpeechToTextResponse]:
    try:
        print("\n" + "=" * 60)
        print("[API] STT 분석 요청 수신")
        print("=" * 60)
        print(f"Presigned URL: {request.presigned_url[:50]}...")
        print(f"정답: {request.answer}")
        print(f"언어: {request.language}")

        # ===== 1단계: 언어 코드 변환 =====
        # "ko-KR" → "ko" (Whisper API는 2글자 코드 사용)
        language_code = request.language.split('-')[0] if request.language else "ko"

        # ===== 2단계: 음성 분석 서비스 호출 =====
        analysis_result = speech_analysis_service.analyze_speech(
            presigned_url=request.presigned_url,
            answer=request.answer,
            language=language_code
        )

        # ===== 3단계: 응답 데이터 생성 =====
        # SpeechToTextResponse 모델에 맞게 데이터 구성
        response_data = SpeechToTextResponse(
            text=analysis_result["transcribed_text"],
            is_correct=analysis_result["is_correct"]
        )

        print("\n" + "=" * 60)
        print("[API] 응답 전송")
        print("=" * 60)
        print(f"변환된 텍스트: {response_data.text}")
        print(f"정답 여부: {response_data.is_correct}")

        return ApiResponse.success(message="음성 분석이 완료되었습니다", data=response_data)

    except ValueError as e:
        # 잘못된 요청 데이터 (400 Bad Request)
        print(f"\n❌ [API] 잘못된 요청: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"잘못된 요청입니다: {str(e)}"
        )

    except Exception as e:
        # 서버 내부 오류 (500 Internal Server Error)
        print(f"\n❌ [API] 서버 오류: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"음성 분석 중 오류가 발생했습니다: {str(e)}"
        )