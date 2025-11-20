package com.ssafy.a202.domain.scenario.dto.response;

import com.ssafy.a202.domain.scenario.entity.SeqOption;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 시퀀스 옵션 응답 DTO (이미지 URL 포함 - 하 난이도용)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "시퀀스 선택 옵션 (이미지 포함)")
public class OptionWithImageResponse {

    @Schema(description = "옵션 ID", example = "1")
    private Long optionId;

    @Schema(description = "옵션 번호 (1부터 시작)", example = "1")
    private int optionNumber;

    @Schema(description = "옵션 이미지 PreSigned URL", example = "https://s3.amazonaws.com/bucket/option1.jpg?signature=...")
    private String optionImageUrl;

    @Schema(description = "정답 여부", example = "true")
    private boolean isAnswer;

    /**
     * SeqOption 엔티티를 OptionWithImageResponse로 변환
     */
    public static OptionWithImageResponse from(SeqOption seqOption, String imageUrl) {
        return OptionWithImageResponse.builder()
                .optionId(seqOption.getId())
                .optionNumber(seqOption.getOptionNo())
                .optionImageUrl(imageUrl)
                .isAnswer(seqOption.isAnswer())
                .build();
    }
}