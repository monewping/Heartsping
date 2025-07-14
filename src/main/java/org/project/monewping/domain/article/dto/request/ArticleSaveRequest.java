package org.project.monewping.domain.article.dto.request;

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
    UUID interestId,
    String source,
    String originalLink,
    String title,
    String summary,
    LocalDateTime publishedAt
) {

}
