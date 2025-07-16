package org.project.monewping.domain.article.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.project.monewping.domain.article.dto.request.ArticleSaveRequest;
import org.project.monewping.domain.article.fetcher.YonhapRssFetcher;

// RssFetcher의 내부 골조는 같기에 대표 클래스로 YonhapRssFetcher를 이용하여 테스트
public class RssFetcherTest {

    private HttpClient httpClientMock;
    private YonhapRssFetcher yonhapRssFetcher;
    private HttpResponse<String> httpResponseMock;

    private static final String MOCK_RSS_XML = """
        <?xml version="1.0" encoding="UTF-8"?>
        <rss version="2.0">
          <channel>
            <item>
              <title>AI 혁신 뉴스</title>
              <link>https://news.yonhap.com/article1</link>
              <description>AI 관련 뉴스 내용</description>
              <pubDate>Wed, 16 Jul 2025 10:00:00 +0900</pubDate>
            </item>
            <item>
              <title>다른 뉴스</title>
              <link>https://news.yonhap.com/article2</link>
              <description>기술 관련 내용</description>
              <pubDate>Wed, 16 Jul 2025 11:00:00 +0900</pubDate>
            </item>
          </channel>
        </rss>
        """;

    @BeforeEach
    void setUp() throws Exception {
        httpClientMock = mock(HttpClient.class);
        httpResponseMock = mock(HttpResponse.class);

        // HTTP 응답 모킹
        when(httpResponseMock.statusCode()).thenReturn(200);
        when(httpResponseMock.body()).thenReturn(MOCK_RSS_XML);

        // HttpClient 모킹 - 어떤 HttpRequest든 위 응답 반환
        when(httpClientMock.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(httpResponseMock);

        // 모킹된 HttpClient 주입하여 테스트 대상 생성
        yonhapRssFetcher = new YonhapRssFetcher(httpClientMock);
    }

    @Test
    @DisplayName("fetch() : 키워드와 일치하는 기사만 반환한다")
    void fetch_ShouldReturnOnlyArticlesThatMatchKeyword() {
        List<ArticleSaveRequest> articles = yonhapRssFetcher.fetch("AI");

        // "AI" 키워드가 제목 또는 설명에 포함된 기사만 반환됨
        assertNotNull(articles);
        assertEquals(1, articles.size());

        ArticleSaveRequest article = articles.get(0);
        assertEquals("https://news.yonhap.com/article1", article.originalLink());
        assertTrue(article.title().contains("AI"));
    }

    @Test
    @DisplayName("fetch() : HTTP 상태 코드가 200이 아니면 빈 리스트 반환")
    void fetch_ShouldReturnEmptyListOnHttpError() throws Exception {
        when(httpResponseMock.statusCode()).thenReturn(500);

        List<ArticleSaveRequest> articles = yonhapRssFetcher.fetch("AI");

        assertNotNull(articles);
        assertTrue(articles.isEmpty());
    }

    @Test
    @DisplayName("fetch() : 예외 발생 시 빈 리스트 반환")
    void fetch_ShouldReturnEmptyListOnException() throws Exception {
        when(httpClientMock.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenThrow(new RuntimeException("HTTP error"));

        List<ArticleSaveRequest> articles = yonhapRssFetcher.fetch("AI");

        assertNotNull(articles);
        assertTrue(articles.isEmpty());
    }
}