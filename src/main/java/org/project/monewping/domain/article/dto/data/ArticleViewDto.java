package org.project.monewping.domain.article.dto.data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 뉴스 기사 조회 정보 DTO
 *
 * @param id                 조회 기록 ID (UUID)
 * @param viewedBy           조회한 사용자 ID (UUID)
 * @param createdAt          조회 시각
 * @param articleId          뉴스 기사 ID (UUID)
 * @param source             뉴스 기사 출처
 * @param sourceUrl          뉴스 기사 원본 URL
 * @param articleTitle       뉴스 기사 제목
 * @param articlePublishedAt 뉴스 기사 발행일시
 * @param articleSummary     뉴스 기사 요약 내용
 * @param articleCommentCount 뉴스 기사 댓글 수
 * @param articleViewCount   뉴스 기사 조회 수
 */
public record ArticleViewDto(

    @NotNull(message = "id는 필수입니다.")
    UUID id,

    @NotNull(message = "조회한 사용자 ID는 필수입니다.")
    UUID viewedBy,

    @NotNull(message = "조회한 날짜는 필수입니다")
    LocalDateTime createdAt,


    @NotNull(message = "조회하는 뉴스 기사 ID는 필수입니다")
    UUID articleId,

    @NotNull(message = "뉴스 기사 출처는 필수입니다")
    @Size(max = 10)
    String source,

    @NotBlank(message = "뉴스 기사 원본은 필수입니다")
    @Size(max = 300)
    String sourceUrl,

    @NotBlank(message = "뉴스 기사 제목은 필수입니다")
    @Size(max = 100)
    String articleTitle,

    @NotNull(message = "뉴스 기사 발행일시는 필수입니다")
    LocalDateTime articlePublishedAt,

    @NotBlank(message = "뉴스 기사 요약은 필수 기재 사항입니다")
    String articleSummary,

    @Min(value = 0)
    long articleCommentCount,

    @Min(value = 0)
    long articleViewCount
) {

}
