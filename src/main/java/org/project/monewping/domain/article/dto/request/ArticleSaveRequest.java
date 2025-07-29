package org.project.monewping.domain.article.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 뉴스 기사 저장 요청용 DTO
 *
 * @param interestId   뉴스 기사 관심사 ID
 * @param source       기사 출처 (예: "TechCrunch")
 * @param originalLink 기사 원본 링크 (중복 체크 기준)
 * @param title        기사 제목
 * @param summary      기사 요약
 * @param publishedAt  기사 발행 일시
 */
public record ArticleSaveRequest(

    @NotNull(message = "관심사 ID는 필수입니다.")
    UUID interestId,

    @NotBlank(message = "출처는 공백일 수 없습니다.")
    @Size(max = 30, message = "출처는 최대 30자까지 허용됩니다.")
    String source,

    @NotBlank(message = "원본 기사 링크는 공백일 수 없습니다")
    @Size(max = 500, message = "원본 기사 링크는 최대 500자까지 허용됩니다.")
    String originalLink,

    @NotBlank(message = "제목은 공백일 수 없습니다.")
    @Size(max = 100, message = "제목은 최대 100자까지 허용됩니다.")
    String title,

    @NotBlank(message = "기사 내용 요약은 공백일 수 없습니다.")
    String summary,

    @NotNull(message = "기사 발행일은 필수입니다.")
    LocalDateTime publishedAt
) {

}
