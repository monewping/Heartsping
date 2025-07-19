package org.project.monewping.domain.article.fetcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.project.monewping.domain.article.dto.request.ArticleSaveRequest;

@DisplayName("YonhapRssFetcher 테스트")
public class YonhapRssFetcherTest {

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
        when(httpClientMock.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(httpResponseMock);

        yonhapRssFetcher = new YonhapRssFetcher(httpClientMock);
    }

    @Test
    @DisplayName("키워드와 일치하는 기사만 반환한다")
    void fetch_ShouldReturnOnlyArticlesThatMatchKeyword() {
        // Given: 키워드 "AI"가 포함된 기사 존재
        String keyword = "AI";

        // When: fetch 호출
        List<ArticleSaveRequest> articles = yonhapRssFetcher.fetch(keyword);

        // Then: "AI" 포함 기사만 반환
        assertNotNull(articles);
        assertEquals(1, articles.size());

        ArticleSaveRequest article = articles.get(0);
        assertEquals("https://news.yonhap.com/article1", article.originalLink());
        assertTrue(article.title().contains("AI"));
    }

    @Test
    @DisplayName("HTTP 상태 코드가 200이 아니면 빈 리스트 반환")
    void fetch_ShouldReturnEmptyListOnHttpError() throws Exception {
        // Given: 응답 상태 코드 500 설정
        when(httpResponseMock.statusCode()).thenReturn(500);

        // When: fetch 호출
        List<ArticleSaveRequest> articles = yonhapRssFetcher.fetch("AI");

        // Then: 빈 리스트 반환
        assertNotNull(articles);
        assertTrue(articles.isEmpty());
    }

    @Test
    @DisplayName("예외 발생 시 빈 리스트 반환")
    void fetch_ShouldReturnEmptyListOnException() throws Exception {
        // Given: send 호출 시 예외 발생 설정
        when(httpClientMock.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenThrow(new RuntimeException("HTTP error"));

        // When: fetch 호출
        List<ArticleSaveRequest> articles = yonhapRssFetcher.fetch("AI");

        // Then: 빈 리스트 반환
        assertNotNull(articles);
        assertTrue(articles.isEmpty());
    }

    @Test
    @DisplayName("fetch() : 빈 키워드일 경우 모든 기사 반환 또는 필터링 없이 동작")
    void fetch_ShouldReturnAllArticles_WhenKeywordIsEmpty() {
        // Given
        String keyword = "";

        // When
        List<ArticleSaveRequest> articles = yonhapRssFetcher.fetch(keyword);

        // Then
        assertNotNull(articles);
        // MOCK_RSS_XML 내 기사 2개가 모두 반환되어야 함
        assertEquals(2, articles.size());
    }

    @Test
    @DisplayName("fetch() : 키워드가 포함되지 않은 기사는 필터링 된다")
    void fetch_ShouldFilterOutNonMatchingArticles() {
        // Given
        String keyword = "없는키워드";

        // When
        List<ArticleSaveRequest> articles = yonhapRssFetcher.fetch(keyword);

        // Then
        assertNotNull(articles);
        assertTrue(articles.isEmpty());
    }

    @Test
    @DisplayName("fetch() : RSS에 기사 아이템이 없으면 빈 리스트 반환")
    void fetch_ShouldReturnEmptyListWhenNoItems() throws Exception {
        // Given
        String emptyRss = """
        <?xml version="1.0" encoding="UTF-8"?>
        <rss version="2.0">
          <channel>
          </channel>
        </rss>
        """;
        when(httpResponseMock.body()).thenReturn(emptyRss);
        when(httpResponseMock.statusCode()).thenReturn(200);

        // When
        List<ArticleSaveRequest> articles = yonhapRssFetcher.fetch("AI");

        // Then
        assertNotNull(articles);
        assertTrue(articles.isEmpty());
    }

    @Test
    @DisplayName("fetch() : pubDate 파싱 실패 시에도 예외 없이 처리")
    void fetch_ShouldHandleInvalidPubDateFormat() throws Exception {
        // Given
        String invalidDateRss = """
        <?xml version="1.0" encoding="UTF-8"?>
        <rss version="2.0">
          <channel>
            <item>
              <title>AI 혁신 뉴스</title>
              <link>https://news.yonhap.com/article1</link>
              <description>AI 관련 뉴스 내용</description>
              <pubDate>잘못된 날짜 포맷</pubDate>
            </item>
          </channel>
        </rss>
        """;
        when(httpResponseMock.body()).thenReturn(invalidDateRss);
        when(httpResponseMock.statusCode()).thenReturn(200);

        // When
        List<ArticleSaveRequest> articles = yonhapRssFetcher.fetch("AI");

        // Then
        assertNotNull(articles);
        assertEquals(1, articles.size());
    }

    @Test
    @DisplayName("fetch() : RSS XML에 <channel> 태그가 없으면 빈 리스트 반환")
    void fetch_ShouldReturnEmptyListWhenNoChannel() throws Exception {
        String noChannelXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><rss version=\"2.0\"></rss>";
        when(httpResponseMock.body()).thenReturn(noChannelXml);
        when(httpResponseMock.statusCode()).thenReturn(200);

        List<ArticleSaveRequest> articles = yonhapRssFetcher.fetch("AI");

        assertNotNull(articles);
        assertTrue(articles.isEmpty());
    }

    @Test
    @DisplayName("fetch() : null 키워드 입력 시 예외 없이 처리")
    void fetch_ShouldHandleNullKeyword() {
        List<ArticleSaveRequest> articles = yonhapRssFetcher.fetch(null);

        assertNotNull(articles);
        // 추가로 모든 기사 반환 또는 필터링 없이 동작 확인
    }

    @Test
    @DisplayName("fetch() : 키워드 대소문자 구분 없이 필터링")
    void fetch_ShouldFilterIgnoringCase() {
        List<ArticleSaveRequest> articlesUpper = yonhapRssFetcher.fetch("AI");
        List<ArticleSaveRequest> articlesLower = yonhapRssFetcher.fetch("ai");

        assertNotNull(articlesUpper);
        assertFalse(articlesUpper.isEmpty());

        assertNotNull(articlesLower);
        assertFalse(articlesLower.isEmpty());

        // 같은 링크를 포함하는지 비교
        assertEquals(articlesUpper.get(0).originalLink(), articlesLower.get(0).originalLink());
    }

    @Test
    @DisplayName("fetch() : 복수 아이템 중 일부만 키워드 포함 시 필터링 정상 동작")
    void fetch_ShouldFilterPartialMatchingItems() {
        List<ArticleSaveRequest> articles = yonhapRssFetcher.fetch("혁신");

        assertNotNull(articles);
        assertEquals(1, articles.size());
        assertTrue(articles.get(0).title().contains("혁신"));
    }

    @Test
    @DisplayName("fetch() : HttpRequest가 올바르게 생성되는지 확인")
    void fetch_ShouldCreateCorrectHttpRequest() throws Exception {
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);

        yonhapRssFetcher.fetch("AI");

        verify(httpClientMock, times(1)).send(requestCaptor.capture(), any());

        HttpRequest capturedRequest = requestCaptor.getValue();
        assertNotNull(capturedRequest);
        assertTrue(capturedRequest.uri().toString().contains("yonhap"));
        assertEquals("GET", capturedRequest.method());
    }
}