package org.project.monewping.domain.article.fetcher.naver.dto;

import java.util.List;

/**
 * 네이버 뉴스 API 응답의 루트 객체를 나타냄
 * <p>
 * 실제 기사 리스트는 items 필드에 포함됨
 * </p>
 *
 * @param items 기사 항목 리스트
 */
public record NaverNewsResponse(
    List<NaverNewsItem> items
) {

}
