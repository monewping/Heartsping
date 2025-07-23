package org.project.monewping.domain.article.dto.data;


/**
 * 네이버 뉴스 API 응답 항목을 나타내는 레코드입니다.
 * <p>
 * 각 필드는 네이버 API의 JSON 응답에서 매핑되며, 기사 제목, 링크, 요약, 발행일 등을 포함합니다.
 * </p>
 *
 * @param title         기사 제목 ( HTML 태그 포함 가능 )
 * @param originalLink  원문 링크 ( 네이버 뉴스 링크 URL )
 * @param description   기사 요약 ( HTML 태그 포함 가능 )
 */
public record NaverNewsItem(
    String title,
    String originalLink,
    String description
) {

}
