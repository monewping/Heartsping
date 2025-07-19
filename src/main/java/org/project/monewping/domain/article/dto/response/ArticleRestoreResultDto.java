package org.project.monewping.domain.article.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 뉴스 기사 복구 결과를 나타내는 DTO입니다.
 *
 * <p>복구가 수행된 시각과 실제 복구된 기사 ID 목록, 복구된 기사 수를 포함합니다.</p>
 *
 * <p>날짜 범위 복구 작업 시 각 날짜별 복구 결과를 표현합니다.</p>
 *
 * @param restoreDate      복구가 수행된 시각
 * @param restoredArticleIds 복구된 뉴스 기사들의 UUID 문자열 목록
 * @param restoredArticleCount 복구된 기사 수
 */
public record ArticleRestoreResultDto(
    LocalDateTime restoreDate,
    List<String> restoredArticleIds,
    int restoredArticleCount
) {

}
