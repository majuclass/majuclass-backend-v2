package com.ssafy.a202.domain.scenario.dto.response;

import com.ssafy.a202.domain.scenario.entity.SeqOption;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 옵션 생성 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
@Schema(description = "옵션 생성 응답")
public class OptionCreateResponse {

    @Schema(description = "옵션 ID", example = "1")
    private Long optionId;

    @Schema(description = "옵션 번호", example = "1")
    private int optionNumber;

    @Schema(description = "옵션 텍스트", example = "안녕하세요")
    private String optionText;

    @Schema(description = "옵션 이미지 URL (이미지가 없으면 null)")
    private String optionImageUrl;

    @Schema(description = "정답 여부", example = "true")
    private boolean isAnswer;

    /**
     * Entity에서 Response DTO로 변환
     */
    public static OptionCreateResponse from(SeqOption option) {
        return OptionCreateResponse.builder()
                .optionId(option.getId())
                .optionNumber(option.getOptionNo())
                .optionText(option.getOptionText())
                .optionImageUrl(option.getOptionS3Key())  // S3 키를 그대로 반환 (Service에서 URL로 변환)
                .isAnswer(option.isAnswer())
                .build();
    }
}