package org.project.monewping.domain.article.fetcher;

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

    @Value("${naver.api.client-id}")
    private String clientId;

    @Value("${naver.api.client-secret}")
    private String clientSecret;

    private static final String NAVER_NEWS_SEARCH_API = "https://openapi.naver.com/v1/search/news.json";

    @Override
    public List<ArticleSaveRequest> fetch(String keyword) {
        log.info("📰 [NaverFetcher] 뉴스 기사 수집 시작 - keyword: {}", keyword);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String uri = UriComponentsBuilder.fromHttpUrl(NAVER_NEWS_SEARCH_API)
            .queryParam("query", keyword)
            .queryParam("display", 10)
            .queryParam("sort", "date")
            .toUriString();

        log.debug("🔗 [NaverFetcher] 호출 URI: {}", uri);
        log.debug("🔐 [NaverFetcher] 헤더: X-Naver-Client-Id={}, X-Naver-Client-Secret={}", mask(clientId), mask(clientSecret));

        try {
            ResponseEntity<NaverNewsResponse> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                entity,
                NaverNewsResponse.class
            );

            log.debug("✅ [NaverFetcher] 응답 수신 - Status: {}", response.getStatusCode());

            if (response.getBody() == null || response.getBody().items() == null) {
                log.warn("⚠️ [NaverFetcher] 응답은 성공했지만 뉴스 아이템이 비어 있습니다.");
                return List.of();
            }

            List<ArticleSaveRequest> results = response.getBody().items().stream()
                .map(item -> {
                    log.debug("📄 [NaverFetcher] 기사 변환 - title: {}, publishedAt: {}", item.title(), item.getPublishedAt());
                    return new ArticleSaveRequest(
                        null,
                        "Naver",
                        item.originalLink(),
                        item.title(),
                        item.description(),
                        item.getPublishedAt()
                    );
                })
                .toList();

            log.info("📦 [NaverFetcher] 기사 {}건 수집 완료", results.size());
            return results;

        } catch (Exception e) {
            log.error("❌ [NaverFetcher] Naver 뉴스 API 호출 실패", e);
            return List.of();
        }
    }

    // 보안 로그용 마스킹 함수
    private String mask(String input) {
        if (input == null || input.length() <= 4) return "****";
        return input.substring(0, 2) + "****" + input.substring(input.length() - 2);
    }
}