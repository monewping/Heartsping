package org.project.monewping.domain.article.fetcher;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.project.monewping.domain.article.dto.data.NaverNewsItem;
import org.project.monewping.domain.article.dto.request.ArticleSaveRequest;
import org.project.monewping.domain.article.dto.response.NaverNewsResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@DisplayName("NaverArticleFetcher 테스트")
public class NaverArticleFetcherTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private NaverArticleFetcher fetcher;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("정상 응답 시 키워드 리스트 중 하나라도 포함된 기사만 반환해야 한다")
    void fetch_shouldReturnFilteredArticles() {
        // given
        UUID interestId = UUID.randomUUID();
        List<String> keywords = List.of("AI", "경제");

        NaverNewsItem item1 = new NaverNewsItem(
            "AI changes the world",
            "http://original.com/1",
            "AI is everywhere"
        );
        NaverNewsItem item2 = new NaverNewsItem(
            "경제 뉴스 속보",
            "http://original.com/2",
            "경제 성장률 발표"
        );
        NaverNewsItem item3 = new NaverNewsItem(
            "Sports news",
            "http://original.com/3",
            "No relevant keywords"
        );

        NaverNewsResponse responseBody = new NaverNewsResponse(List.of(item1, item2, item3));

        ResponseEntity<NaverNewsResponse> responseEntity =
            new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(NaverNewsResponse.class)
        )).thenReturn(responseEntity);

        // when
        List<ArticleSaveRequest> result = fetcher.fetch(interestId, keywords);

        // then
        assertThat(result).extracting(ArticleSaveRequest::originalLink)
            .containsExactlyInAnyOrder("http://original.com/1", "http://original.com/2");
        assertThat(result).extracting(ArticleSaveRequest::originalLink)
            .doesNotContain("http://original.com/3");
    }

    @Test
    @DisplayName("응답 실패 시 빈 리스트 반환")
    void fetch_shouldReturnEmptyListOnErrorStatus() {
        // given
        UUID interestId = UUID.randomUUID();
        List<String> keywords = List.of("AI");

        ResponseEntity<NaverNewsResponse> responseEntity =
            new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);

        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(NaverNewsResponse.class)
        )).thenReturn(responseEntity);

        // when
        List<ArticleSaveRequest> result = fetcher.fetch(interestId, keywords);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("예외 발생 시 빈 리스트 반환")
    void fetch_shouldReturnEmptyListOnException() {
        // given
        UUID interestId = UUID.randomUUID();
        List<String> keywords = List.of("AI");

        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(NaverNewsResponse.class)
        )).thenThrow(new RuntimeException("Network error"));

        // when
        List<ArticleSaveRequest> result = fetcher.fetch(interestId, keywords);

        // then
        assertThat(result).isEmpty();
    }
}