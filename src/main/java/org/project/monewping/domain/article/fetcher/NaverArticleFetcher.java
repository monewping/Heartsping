package org.project.monewping.domain.article.fetcher;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.monewping.domain.article.dto.request.ArticleSaveRequest;
import org.project.monewping.domain.article.dto.response.NaverNewsResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

/**
 * 네이버 뉴스 API를 통해 뉴스를 수집하는 구현체입니다.
 *
 * <p>
 * Naver Open API 사용: https://developers.naver.com/docs/serviceapi/search/news/news.md
 * 검색 키워드를 기준으로 기사 데이터를 가져와 내부 저장 요청 DTO인 {@link ArticleSaveRequest}로 변환합니다.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NaverArticleFetcher implements ArticleFetcher {

    private final RestTemplate restTemplate;

    // 네이버 API 클라이언트 ID
    @Value("${naver.api.client-id}")
    private String clientId;

    // 네이버 API 클라이언트 시크릿
    @Value("${naver.api.client-secret}")
    private String clientSecret;

    private static final String NAVER_NEWS_SEARCH_API = "https://openapi.naver.com/v1/search/news.json";

    /**
     * Fetches news articles from the Naver News API using the given keyword and converts them into a list of internal DTOs.
     *
     * @param keyword the search keyword to query news articles for
     * @return a list of {@link ArticleSaveRequest} objects representing the fetched news articles; returns an empty list if the API call fails or no articles are found
     */
    @Override
    public List<ArticleSaveRequest> fetch(String keyword) {
        log.info("Naver 뉴스 기사 수집 시작 = keyword : {}", keyword);

        // 요청 헤더 구성
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // URI 구성
        String uri = UriComponentsBuilder.fromHttpUrl(NAVER_NEWS_SEARCH_API)
            .queryParam("query", keyword)
            .queryParam("display", 10)
            .queryParam("sort", "date")
            .toUriString();

        try {
            // 네이버 뉴스 검색 API 호출
            ResponseEntity<NaverNewsResponse> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                entity,
                NaverNewsResponse.class
            );

            // 응답 결과 파싱 후 ArticleSaveRequest 리스트로 변환
            return Optional.ofNullable(response.getBody())
                .map(NaverNewsResponse::items)
                .orElse(List.of())
                .stream()
                .map(item -> new ArticleSaveRequest(
                    // InterestId는 이후 외부 주입
                    null,
                    "Naver",
                    item.originalLink(),
                    item.title(),
                    item.description(),
                    item.getPublishedAt()
                ))
                .toList();

        } catch (Exception e) {
            log.error("Naver 뉴스 API 호출 실패", e);
            return List.of();
        }

    }
}
