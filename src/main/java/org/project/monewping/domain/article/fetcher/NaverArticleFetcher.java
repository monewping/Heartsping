package org.project.monewping.domain.article.fetcher;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.monewping.domain.article.dto.data.NaverNewsItem;
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
 * Naver Open API 문서: https://developers.naver.com/docs/serviceapi/search/news/news.md
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

    private static final String NAVER_API_URL = "https://openapi.naver.com/v1/search/news.json";
    private static final int MAX_TOTAL_COUNT = 1000;
    private static final int DISPLAY_COUNT = 100;

    /**
     * 네이버 뉴스 API를 통해 주어진 키워드의 뉴스 기사를 수집합니다.
     *
     * @param interestId 관심사 ID
     * @param keywords    검색 키워드
     * @return {@link ArticleSaveRequest} 리스트
     */
    @Override
    public List<ArticleSaveRequest> fetch(UUID interestId, List<String> keywords) {

        List<ArticleSaveRequest> result = new ArrayList<>();

        // 여러 번 요청 (최대 300개, 50개씩)
        for (int start = 1; start <= MAX_TOTAL_COUNT; start += DISPLAY_COUNT) {
            // 요청 URL 생성
            String url = UriComponentsBuilder.fromHttpUrl(NAVER_API_URL)
                .queryParam("query", keywords != null && !keywords.isEmpty() ? keywords.get(0) : "")
                .queryParam("display", DISPLAY_COUNT)
                .queryParam("start", start)
                .build()
                .toUriString();

            // HTTP 헤더 구성
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Naver-Client-Id", clientId);
            headers.set("X-Naver-Client-Secret", clientSecret);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            try {
                // 네이버 뉴스 API 호출
                ResponseEntity<NaverNewsResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), NaverNewsResponse.class
                );

                // 응답 성공 여부 확인
                if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                    log.warn("[ 네이버 뉴스 응답 실패 ] - status = {}, keyword = {}, start = {}", response.getStatusCode(), keywords, start);
                    break;
                }

                // 기사 항목 리스트 추출
                List<NaverNewsItem> items = response.getBody().items();

                // 더 이상 기사가 없다면 종료
                if (items == null || items.isEmpty()) {
                    log.info("더 이상 수집할 기사가 없습니다 - keyword = {}, start = {}", keywords, start);
                    break;
                }

                // 키워드 필터링 및 ArticleSaveRequest로 매핑하여 누적
                List<ArticleSaveRequest> filtered = items.stream()
                    .filter(item -> containsKeyword(item, keywords))
                    .map(item -> new ArticleSaveRequest(
                        interestId,
                        "Naver",
                        item.originalLink(),
                        HtmlCleaner.strip(item.title()),
                        HtmlCleaner.strip(item.description()),
                        LocalDateTime.now()
                    ))
                    .toList();

                result.addAll(filtered);

                // 마지막 요청이 100개 미만이면 더 이상 수집 불필요 (네이버 응답 제한)
                if (items.size() < DISPLAY_COUNT) {
                    break;
                }

            } catch (Exception e) {
                log.error("[ 네이버 뉴스 수집 실패 ] - keyword = {}, start = {}", keywords, start, e);
                break; // 예외 발생 시 반복 중단
            }
        }

        return result;
    }

    /**
     * 주어진 뉴스 항목의 제목 또는 설명에 키워드가 포함되어 있는지 확인합니다.
     *
     * @param item     뉴스 항목
     * @param keywords 검색 키워드
     * @return 키워드 포함 여부
     */
    private boolean containsKeyword(NaverNewsItem item, List<String> keywords) {
        String title = HtmlCleaner.strip(item.title());
        String desc = HtmlCleaner.strip(item.description());

        String lowerTitle = title != null ? title.toLowerCase() : "";
        String lowerDesc = desc != null ? desc.toLowerCase() : "";

        for (String keyword : keywords) {
            if (keyword == null || keyword.isBlank()) continue;
            String lowerKeyword = keyword.toLowerCase();
            if (lowerTitle.contains(lowerKeyword) || lowerDesc.contains(lowerKeyword)) {
                return true;
            }
        }
        return false;
    }
}