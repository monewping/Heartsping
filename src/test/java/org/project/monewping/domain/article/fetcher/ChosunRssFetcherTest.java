package org.project.monewping.domain.article.fetcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

@DisplayName("ChosunRssFetcher 테스트")
public class ChosunRssFetcherTest {

    private HttpClient mockClient;
    private HttpResponse<String> mockResponse;
    private ChosunRssFetcher fetcher;

    private static final String SAMPLE_RSS_XML = """
        <?xml version="1.0" encoding="UTF-8" ?>
        <rss version="2.0">
          <channel>
            <item>
              <title>테스트 제목 1</title>
              <link>https://chosun.com/article1</link>
              <description>테스트 내용 1</description>
              <pubDate>Wed, 16 Jul 2025 15:00:00 +0900</pubDate>
            </item>
            <item>
              <title>키워드 포함 제목</title>
              <link>https://chosun.com/article2</link>
              <description>키워드 포함 내용</description>
              <pubDate>Wed, 16 Jul 2025 16:00:00 +0900</pubDate>
            </item>
          </channel>
        </rss>
        """;

    @BeforeEach
    void setUp() throws Exception {
        mockClient = mock(HttpClient.class);
        mockResponse = mock(HttpResponse.class);
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(mockResponse);

        fetcher = new ChosunRssFetcher(mockClient);
    }

    @Test
    @DisplayName("키워드가 빈 문자열일 때, 모든 기사 목록을 반환한다")
    void fetch_ReturnsAllArticles_WhenKeywordIsEmpty() throws Exception {
        // Given: HTTP 응답 코드가 200이고, RSS에 2개의 기사가 포함되어 있음
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(SAMPLE_RSS_XML);

        // When: fetch 메서드를 빈 키워드("")로 호출하면
        List<ArticleSaveRequest> articles = fetcher.fetch("");

        // Then: 전체 기사 2개가 반환된다
        assertEquals(2, articles.size());

        ArticleSaveRequest first = articles.get(0);
        assertEquals("테스트 제목 1", first.title());
        assertEquals("https://chosun.com/article1", first.originalLink());
        assertEquals("테스트 내용 1", first.summary());

        ArticleSaveRequest second = articles.get(1);
        assertEquals("키워드 포함 제목", second.title());
    }

    @Test
    @DisplayName("키워드가 주어지면, 제목이나 요약에 키워드가 포함된 기사만 반환한다")
    void fetch_ReturnsFilteredArticles_WhenKeywordProvided() throws Exception {
        // Given: HTTP 응답이 200이며, 기사 중 하나에 '키워드'가 포함됨
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(SAMPLE_RSS_XML);

        // When: fetch 메서드를 '키워드'로 호출하면
        List<ArticleSaveRequest> articles = fetcher.fetch("키워드");

        // Then: 해당 키워드를 포함한 기사만 반환된다
        assertEquals(1, articles.size());
        assertTrue(articles.get(0).title().contains("키워드") || articles.get(0).summary().contains("키워드"));
    }

    @Test
    @DisplayName("HTTP 응답 상태가 200이 아니면 빈 리스트를 반환한다")
    void fetch_ReturnsEmptyList_WhenResponseStatusIsNot200() throws Exception {
        // Given: 응답 상태 코드가 500이고 본문은 비어 있음
        when(mockResponse.statusCode()).thenReturn(500);
        when(mockResponse.body()).thenReturn("");

        // When: fetch 메서드를 호출하면
        List<ArticleSaveRequest> articles = fetcher.fetch("키워드");

        // Then: 빈 리스트가 반환된다
        assertTrue(articles.isEmpty());
    }

    @Test
    @DisplayName("HTTP 요청 중 예외가 발생하면 빈 리스트를 반환한다")
    void fetch_ReturnsEmptyList_WhenExceptionThrown() throws Exception {
        // Given: HttpClient가 예외를 발생시키도록 설정됨
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenThrow(new RuntimeException("HTTP 요청 실패"));

        // When: fetch 메서드를 호출하면
        List<ArticleSaveRequest> articles = fetcher.fetch("키워드");

        // Then: 예외가 발생해도 빈 리스트가 반환된다
        assertTrue(articles.isEmpty());
    }
}