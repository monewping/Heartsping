package org.project.monewping.domain.article.fetcher;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.project.monewping.domain.article.dto.request.ArticleSaveRequest;

@DisplayName("RSS 기사 수집기 테스트")
public class RssArticleFetcherTest {

    private static final String RSS_SAMPLE = """
        <rss version="2.0">
          <channel>
            <item>
              <title>AI is changing the world</title>
              <link>http://example.com/article1</link>
              <description>Artificial Intelligence impacts everything</description>
              <pubDate>Mon, 15 Jul 2024 10:00:00 GMT</pubDate>
            </item>
            <item>
              <title>Stock market update</title>
              <link>http://example.com/article2</link>
              <description>Daily stock market summary</description>
              <pubDate>Mon, 15 Jul 2024 11:00:00 GMT</pubDate>
            </item>
          </channel>
        </rss>
        """;

    @Test
    @DisplayName("키워드를 포함한 기사만 필터링하여 반환해야 한다")
    void shouldFilterArticlesByKeyword() throws Exception {
        // given
        UUID interestId = UUID.randomUUID();
        HttpClient mockClient = mockHttpClientReturning(RSS_SAMPLE, 200);
        RssArticleFetcher fetcher = new TestRssFetcher(mockClient);

        // when
        List<ArticleSaveRequest> result = fetcher.fetch(interestId, "ai");

        // then
        assertThat(result).hasSize(2);

        // 첫 번째 기사
        assertThat(result.get(0).title()).containsIgnoringCase("AI");
        assertThat(result.get(0).originalLink()).isEqualTo("http://example.com/article1");
        assertThat(result.get(0).source()).isEqualTo("TestNews");

        // 두 번째 기사도 키워드 포함 확인 (title 또는 summary 중 하나라도)
        assertThat(result.get(1).title()).isEqualTo("Stock market update");
        assertThat(result.get(1).originalLink()).isEqualTo("http://example.com/article2");
        assertThat(result.get(1).source()).isEqualTo("TestNews");
    }


    @Test
    @DisplayName("HTTP 200이 아닌 경우 빈 리스트를 반환해야 한다")
    void shouldReturnEmptyListWhenStatusIsNotOk() throws Exception {
        // given
        HttpClient mockClient = mockHttpClientReturning("error", 500);
        RssArticleFetcher fetcher = new TestRssFetcher(mockClient);

        // when
        List<ArticleSaveRequest> result = fetcher.fetch(UUID.randomUUID(), "ai");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("응답 파싱 중 예외가 발생해도 빈 리스트를 반환해야 한다")
    void shouldReturnEmptyListOnException() throws Exception {
        // given
        HttpClient mockClient = mockHttpClientThrowing();
        RssArticleFetcher fetcher = new TestRssFetcher(mockClient);

        // when
        List<ArticleSaveRequest> result = fetcher.fetch(UUID.randomUUID(), "ai");

        // then
        assertThat(result).isEmpty();
    }

    // ========== 헬퍼 클래스 및 메서드 ==========

    private static class TestRssFetcher extends RssArticleFetcher {
        public TestRssFetcher(HttpClient client) {
            super(client);
        }

        @Override
        protected String rssFeedUrl() {
            return "http://mocked-url.com/rss";
        }

        @Override
        protected String sourceName() {
            return "TestNews";
        }
    }

    private HttpClient mockHttpClientReturning(String body, int statusCode) throws Exception {
        HttpClient client = Mockito.mock(HttpClient.class);
        HttpResponse<String> response = Mockito.mock(HttpResponse.class);
        Mockito.when(response.statusCode()).thenReturn(statusCode);
        Mockito.when(response.body()).thenReturn(body);
        Mockito.when(client.send(Mockito.any(HttpRequest.class), Mockito.any(HttpResponse.BodyHandler.class)))
            .thenReturn(response);
        return client;
    }

    private HttpClient mockHttpClientThrowing() throws Exception {
        HttpClient client = Mockito.mock(HttpClient.class);
        Mockito.when(client.send(Mockito.any(HttpRequest.class), Mockito.any(HttpResponse.BodyHandler.class)))
            .thenThrow(new RuntimeException("Network error"));
        return client;
    }

}
