package org.project.monewping.domain.article.dto.data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 뉴스 기사 정보를 담는 DTO (Data Transfer Object) 입니다.
 *
 * @param id           뉴스 기사 고유 식별자(UUID)
 * @param source       뉴스 출처명 (예: 중앙일보, 조선일보 등)
 * @param sourceUrl    뉴스 기사 원본 링크(URL)
 * @param title        뉴스 기사 제목
 * @param publishDate  뉴스 기사 발행일시
 * @param summary      뉴스 기사 요약
 * @param commentCount 댓글 수
 * @param viewCount    조회 수
 * @param viewedByMe   현재 사용자가 해당 기사를 조회했는지 여부
 */
public record ArticleDto(
    UUID id,
    String source,
    String sourceUrl,
    String title,
    LocalDateTime publishDate,
    String summary,
    Long commentCount,
    Long viewCount,
    boolean viewedByMe
) {
    public ArticleDto withViewedByMe(boolean viewedByMe) {
        return new ArticleDto(
            id, source, sourceUrl, title, publishDate,
            summary, commentCount, viewCount, viewedByMe
        );
    }
}
