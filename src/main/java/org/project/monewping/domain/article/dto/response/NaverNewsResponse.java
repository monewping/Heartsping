package org.project.monewping.domain.article.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.project.monewping.domain.article.dto.data.NaverNewsItem;

/**
 * 네이버 뉴스 API 응답을 매핑하는 DTO 클래스입니다.
 *
 * <p>네이버 뉴스 검색 API 호출 결과에서 뉴스 기사 목록( items )만을 포함합니다.</p>
 * <p>알 수 없는 JSON 프로퍼티는 무시하도록 설정되어 있습니다.</p>
 *
 * @param items 뉴스 기사 항목 리스트
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record NaverNewsResponse(
    List<NaverNewsItem> items
) {

}
