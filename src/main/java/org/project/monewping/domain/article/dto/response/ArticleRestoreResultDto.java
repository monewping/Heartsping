package org.project.monewping.domain.article.dto.response;

import java.time.LocalDate;
import java.util.List;

/**
 * 뉴스 기사 복구 결과를 나타내는 DTO입니다.
 *
 * 복구 대상 날짜와 실제 복구된 기사 ID 목록, 복구된 기사 수를 포함합니다.
 *
 * <p>예를 들어, 특정 날짜 기준으로 백업 파일을 불러와
 * 현재 DB에 존재하지 않는 기사만 저장하고 그 결과를 반환할 때 사용됩니다.</p>
 *
 * @param restoredDate 복구가 수행된 날짜 기준 (보통 백업 기준 날짜)
 * @param restoredArticleIds 복구된 기사들의 UUID 문자열 목록
 * @param restoredArticleCount 복구된 기사 수
 */
public record ArticleRestoreResultDto(
    LocalDate restoredDate,
    List<String> restoredArticleIds,
    int restoredArticleCount
) {

}
