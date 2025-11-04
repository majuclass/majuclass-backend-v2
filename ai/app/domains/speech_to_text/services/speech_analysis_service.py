from app.domains.speech_to_text.utils.s3_downloader import S3Downloader
from app.domains.speech_to_text.services.stt_service import STTService
from app.domains.speech_to_text.services.similarity_service import SimilarityService
from typing import Dict, Any, Optional
import logging

class SpeechAnalysisService:
    def __init__(
        self,
        s3_downloader: Optional[S3Downloader] = None,
        stt_service: Optional[STTService] = None,
        similarity_service: Optional[SimilarityService] = None
    ) -> None:
        self.s3_downloader = s3_downloader or S3Downloader()
        self.stt_service = stt_service or STTService()
        self.similarity_service = similarity_service or SimilarityService()

        logging.info("[SpeechAnalysisService] 서비스 초기화 완료")

    def analyze_speech(
        self,
        presigned_url: str,
        answer: str,
        language: str = "ko"
    ) -> Dict[str, Any]:

        temp_file_path = None

        try:
            print("=" * 50)
            print("[SpeechAnalysisService] 음성 분석 시작")
            print("=" * 50)

            # ===== 1단계: S3에서 음성 파일 다운로드 =====
            print("\n[1/4] S3 파일 다운로드 중...")
            temp_file_path, file_name = self.s3_downloader.download_from_presigned_url(
                presigned_url
            )
            print(f"✓ 다운로드 완료: {file_name}")

            # ===== 2단계: STT로 음성을 텍스트로 변환 =====
            print("\n[2/4] 음성을 텍스트로 변환 중...")
            transcribed_text = self.stt_service.transcribe_audio(
                audio_file_path=temp_file_path,
                language=language
            )
            print(f"✓ 변환 완료: '{transcribed_text}'")

            # ===== 3단계: 유사도 비교로 정답 판별 =====
            print("\n[3/4] 정답 여부 판별 중...")
            is_correct, similarity_score = self.similarity_service.is_similar(
                text1=transcribed_text,
                text2=answer
            )
            result_emoji = "✅" if is_correct else "❌"
            print(f"{result_emoji} 판별 완료: {'정답' if is_correct else '오답'} (유사도: {similarity_score:.2%})")

            # ===== 4단계: 결과 정리 =====
            print("\n[4/4] 결과 정리 중...")
            result = {
                "transcribed_text": transcribed_text,    # 아이가 말한 내용
                "is_correct": is_correct,                # 정답 여부
                "similarity_score": round(similarity_score, 4),  # 유사도 점수
                "answer": answer                         # 원본 정답
            }

            print("=" * 50)
            print("[SpeechAnalysisService] 분석 완료")
            print("=" * 50)

            return result

        except Exception as e:
            # 에러 발생 시 상세 로그 출력
            print(f"\n❌ [SpeechAnalysisService] 분석 실패: {str(e)}")
            raise Exception(f"음성 분석 실패: {str(e)}")

        finally:
            # ===== 정리 작업: 임시 파일 삭제 =====
            # finally 블록: 에러가 나든 안 나든 반드시 실행됨
            # 비유: 식사 후 설거지는 반드시 해야 함
            if temp_file_path:
                print("\n[Cleanup] 임시 파일 삭제 중...")
                self.s3_downloader.cleanup_temp_file(temp_file_path)
                print("✓ 정리 완료")

    def set_similarity_threshold(self, threshold: float) -> None:
        self.similarity_service.set_threshold(threshold)
        print(f"[SpeechAnalysisService] 유사도 임계값 변경: {threshold}")

    def get_service_info(self) -> Dict[str, str]:

        return {
            "s3_downloader": self.s3_downloader.__class__.__name__,
            "stt_service": self.stt_service.__class__.__name__,
            "similarity_service": self.similarity_service.__class__.__name__,
            "similarity_threshold": str(self.similarity_service.similarity_threshold)
        }
