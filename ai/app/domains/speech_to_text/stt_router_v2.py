from fastapi import APIRouter, HTTPException, status, Request, Depends
from sqlalchemy.ext.asyncio import AsyncSession

from app.common.api_response import ApiResponse
from app.common.exceptions import (
    SessionNotFoundError,
    PermissionDeniedError,
    SequenceNotFoundError
)
from app.database import get_session
from app.domains.speech_to_text.adapters.db_repository_adapter import DBRepositoryAdapter
from app.domains.speech_to_text.adapters.s3_storage_adapter import S3StorageAdapter
from app.domains.speech_to_text.adapters.whisper_stt_adapter import WhisperSTTAdapter
from app.domains.speech_to_text.models.evaluation_models import EvaluationRequest
from app.domains.speech_to_text.models.stt_request_models import SpeechToTextRequestModel
from app.domains.speech_to_text.models.stt_response_models import SpeechToTextResponse
from app.domains.speech_to_text.services.evaluation_pipeline import EvaluationPipeline
from app.domains.speech_to_text.services.evaluator_facade import EvaluatorFacade

# Router 생성
router = APIRouter(
    prefix="/ai",
    tags=["STT"],
)


def create_evaluator_facade() -> EvaluatorFacade:
    # Adapters 생성
    storage = S3StorageAdapter()
    stt = WhisperSTTAdapter()
    repository = DBRepositoryAdapter()

    # Pipeline 생성
    pipeline = EvaluationPipeline()

    # Facade 생성
    facade = EvaluatorFacade(
        storage_port=storage,
        stt_port=stt,
        repository_port=repository,
        pipeline=pipeline
    )

    return facade


# Facade 인스턴스
evaluator_facade = create_evaluator_facade()


@router.post(
    "/stt-analyze/{session_id}/{seq_no}",
    response_model=ApiResponse[SpeechToTextResponse],
    summary="음성 평가 (V2 - 새 아키텍처)",
    description="새로운 아키텍처로 음성을 평가합니다. Facade 패턴 사용.",
    status_code=status.HTTP_200_OK
)
async def stt_analyze_v2(
    session_id: int,
    seq_no: int,
    body: SpeechToTextRequestModel,
    req: Request,
    db: AsyncSession = Depends(get_session)
) -> ApiResponse[SpeechToTextResponse]:
    try:
        # 1. 인증 정보
        user_id = req.state.user_id

        # 2. 요청 DTO 생성
        eval_request = EvaluationRequest(
            user_id=user_id,
            session_id=session_id,
            seq_no=seq_no,
            audio_s3_key=body.audio_s3_key,
            language="ko"
        )

        # 3. Facade 호출 (모든 로직은 여기서 처리)
        result = await evaluator_facade.evaluate(eval_request, db)

        # 4. 응답 변환
        response_data = SpeechToTextResponse(
            session_stt_answer_id=result.answer_id,
            transcribed_text=result.transcribed_text,
            answer_text=result.answer_text,
            similarity_score=float(result.scores.weighted),
            is_correct=result.is_correct,
            # feedback_message=result.feedback,
            attempt_no=result.attempt_no
        )

        return ApiResponse.success(
            message="음성 분석이 완료되었습니다.",
            data=response_data
        )

    except (SessionNotFoundError, PermissionDeniedError) as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail=e.message
        )

    except SequenceNotFoundError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=e.message
        )

    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"잘못된 요청입니다: {str(e)}"
        )

    except Exception as e:
        print(f"\n❌ [Router] 예상치 못한 에러: {str(e)}")
        import traceback
        traceback.print_exc()

        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"음성 분석 중 오류가 발생했습니다: {str(e)}"
        )
