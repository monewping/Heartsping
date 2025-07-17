package org.project.monewping.domain.article.fetcher;

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

@DisplayName("HankyungRssFetcher 테스트")
public class HankyungRssFetcherTest {

    private HttpClient httpClientMock;
    private HankyungRssFetcher hankyungRssFetcher;
    private HttpResponse<String> httpResponseMock;

    private static final String MOCK_RSS_XML = """
        <rss>
          <channel>
            <item>
              <title>경제 뉴스 키워드1 포함</title>
              <link>http://example.com/1</link>
              <description>요약 설명 1</description>
              <pubDate>Tue, 15 Jul 2025 14:00:00 +0900</pubDate>
            </item>
            <item>
              <title>경제 뉴스 키워드2 미포함</title>
              <link>http://example.com/2</link>
              <description>요약 설명 2</description>
              <pubDate>Tue, 15 Jul 2025 15:00:00 +0900</pubDate>
            </item>
          </channel>
        </rss>
        """;

    @BeforeEach
    void setUp() throws Exception {
        httpClientMock = mock(HttpClient.class);
        httpResponseMock = mock(HttpResponse.class);

        when(httpResponseMock.statusCode()).thenReturn(200);
        when(httpResponseMock.body()).thenReturn(MOCK_RSS_XML);

        when(httpClientMock.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(httpResponseMock);

        hankyungRssFetcher = new HankyungRssFetcher(httpClientMock);
    }

    @Test
    @DisplayName("fetch() : 키워드와 일치하는 기사만 반환한다")
    void fetch_ShouldReturnOnlyArticlesThatMatchKeyword() {
        // Given: HTTP 응답이 200이고, RSS에 키워드1을 포함한 기사가 존재함
        String keyword = "키워드1";

        // When: fetch() 메서드로 해당 키워드로 기사 조회 시
        List<ArticleSaveRequest> articles = hankyungRssFetcher.fetch(keyword);

        // Then: 키워드가 포함된 기사만 반환된다
        assertNotNull(articles);
        assertEquals(1, articles.size());

        ArticleSaveRequest article = articles.get(0);
        assertEquals("http://example.com/1", article.originalLink());
        assertTrue(article.title().contains("키워드1"));
    }

    @Test
    @DisplayName("fetch() : HTTP 상태 코드가 200이 아니면 빈 리스트 반환")
    void fetch_ShouldReturnEmptyListOnHttpError() throws Exception {
        // Given: HTTP 응답 코드가 500으로 설정됨
        when(httpResponseMock.statusCode()).thenReturn(500);

        // When: fetch() 호출
        List<ArticleSaveRequest> articles = hankyungRssFetcher.fetch("키워드1");

        // Then: 빈 리스트 반환
        assertNotNull(articles);
        assertTrue(articles.isEmpty());
    }

    @Test
    @DisplayName("fetch() : 예외 발생 시 빈 리스트 반환")
    void fetch_ShouldReturnEmptyListOnException() throws Exception {
        // Given: HttpClient가 예외를 던지도록 설정됨
        when(httpClientMock.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenThrow(new RuntimeException("HTTP error"));

        // When: fetch() 호출
        List<ArticleSaveRequest> articles = hankyungRssFetcher.fetch("키워드1");

        // Then: 예외 발생 시에도 빈 리스트 반환
        assertNotNull(articles);
        assertTrue(articles.isEmpty());
    }

}
