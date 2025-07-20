package org.project.monewping.domain.article.dto.request;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 뉴스 기사 검색 요청을 위한 DTO입니다.
 *
 * @param keyword        제목 또는 요약에서 부분일치 검색할 키워드 (null 또는 빈 문자열 가능)
 * @param interestId     관심사 ID (null일 경우 관심사 필터 미적용)
 * @param sourceIn       포함할 뉴스 출처 목록 (null 또는 빈 리스트일 경우 출처 필터 미적용)
 * @param publishDateFrom 발행일 시작 범위 (null일 경우 시작일 제한 없음)
 * @param publishDateTo   발행일 종료 범위 (null일 경우 종료일 제한 없음)
 * @param orderBy        정렬 기준 (publishDate, commentCount, viewCount 중 하나)
 * @param direction      정렬 방향 ("ASC" 또는 "DESC")
 * @param cursor         커서 ID (페이지네이션용, null 가능)
 * @param after          커서 기준 정렬 필드 값 (페이지네이션용, null 가능)
 * @param limit          조회할 최대 개수
 * @param requestUserId  요청 사용자 ID (조회 시 사용자별 조회 여부 판단용, null 가능)
 */
public record ArticleSearchRequest(
    String keyword,
    UUID interestId,
    List<String> sourceIn,
    LocalDateTime publishDateFrom,
    LocalDateTime publishDateTo,
    String orderBy,
    String direction,
    String cursor,
    LocalDateTime after,
    int limit,
    UUID requestUserId
) {

}
