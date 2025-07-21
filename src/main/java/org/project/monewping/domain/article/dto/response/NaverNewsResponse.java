package org.project.monewping.domain.article.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.project.monewping.domain.article.dto.data.NaverNewsItem;

/**
 * 네이버 뉴스 API 응답의 루트 객체를 나타냄
 * <p>
 * 실제 기사 리스트는 items 필드에 포함됨
 * </p>
 *
 * @param items 기사 항목 리스트
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record NaverNewsResponse(
    @JsonProperty("items")
    List<NaverNewsItem> items
) {

}
