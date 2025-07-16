package org.project.monewping.domain.article.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.project.monewping.domain.article.dto.data.NaverNewsItem;
import org.project.monewping.domain.article.dto.request.ArticleSaveRequest;
import org.project.monewping.domain.article.dto.response.NaverNewsResponse;
import org.project.monewping.domain.article.fetcher.NaverArticleFetcher;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class NaverArticleFetcherTest {

    private RestTemplate restTemplate;
    private NaverArticleFetcher naverArticleFetcher;

    @BeforeEach
    void setUp() throws Exception {
        restTemplate = mock(RestTemplate.class);
        naverArticleFetcher = new NaverArticleFetcher(restTemplate);
        setPrivateField(naverArticleFetcher, "clientId", "test-client-id");
        setPrivateField(naverArticleFetcher, "clientSecret", "test-client-secret");
    }

    @Test
    @DisplayName("fetch() : 정상 응답 시 기사 리스트를 반환한다")
    void fetchReturnsArticles_whenApiReturnsValidResponse() {
        // given
        NaverNewsItem item = new NaverNewsItem(
            "뉴스 제목",
            "https://news.link",
            "뉴스 요약",
            "Wed, 16 Jul 2025 10:00:00 +0900"
        );
        NaverNewsResponse responseBody = new NaverNewsResponse(List.of(item));

        ResponseEntity<NaverNewsResponse> responseEntity =
            new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(NaverNewsResponse.class)
        )).thenReturn(responseEntity);

        // when
        List<ArticleSaveRequest> articles = naverArticleFetcher.fetch("테스트키워드");

        // then
        assertNotNull(articles);
        assertEquals(1, articles.size());

        ArticleSaveRequest article = articles.get(0);
        assertEquals("Naver", article.source());
        assertEquals("https://news.link", article.originalLink());
        assertEquals("뉴스 제목", article.title());
        assertEquals("뉴스 요약", article.summary());
        assertNotNull(article.publishedAt());
    }

    @Test
    @DisplayName("fetch() : API 호출 중 예외 발생 시 빈 리스트 반환")
    void fetchReturnsEmptyList_whenApiThrowsException() {
        // given
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(NaverNewsResponse.class)
        )).thenThrow(new RuntimeException("API error"));

        // when
        List<ArticleSaveRequest> articles = naverArticleFetcher.fetch("테스트키워드");

        // then
        assertNotNull(articles);
        assertTrue(articles.isEmpty());
    }


    private void setPrivateField(Object target, String fieldName, Object value) throws Exception{
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

}
