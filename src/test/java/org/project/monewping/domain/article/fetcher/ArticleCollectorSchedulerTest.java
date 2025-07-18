package org.project.monewping.domain.article.fetcher;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.monewping.domain.article.dto.request.ArticleSaveRequest;
import org.project.monewping.domain.article.scheduler.ArticleCollectorScheduler;
import org.project.monewping.domain.article.service.ArticlesService;
import org.project.monewping.domain.interest.entity.Interest;
import org.project.monewping.domain.interest.repository.InterestRepository;

@DisplayName("ArticleCollectorScheduler 테스트")
@ExtendWith(MockitoExtension.class)
public class ArticleCollectorSchedulerTest {

    @Mock
    private InterestRepository interestRepository;

    @Mock
    private ArticlesService articlesService;

    @Mock
    private ArticleFetcher articleFetcher;

    private ArticleCollectorScheduler articleCollectorScheduler;

    @BeforeEach
    void setUp() {
        articleCollectorScheduler = new ArticleCollectorScheduler(
            interestRepository,
            List.of(articleFetcher),
            articlesService
        );
    }

    @Test
    @DisplayName("뉴스 기사 수집 스케줄러가 관심사 키워드 기반으로 Fetch 및 SaveAll을 정상 수행한다")
    void article_Collector_Scheduler_FetchAndSaveAll_Test() throws Exception {
        // Given: 관심사와 해당 관심사로 수집된 기사 리스트
        UUID interestId = UUID.randomUUID();
        Interest interest = createInterestWithId("인공지능", interestId);

        ArticleSaveRequest dummyArticle = new ArticleSaveRequest(
            interestId,
            "Naver",
            "https://example.com/article1",
            "AI is changing the world",
            "요약 내용",
            LocalDateTime.now()
        );

        when(interestRepository.findAll()).thenReturn(List.of(interest));
        when(articleFetcher.fetch("인공지능")).thenReturn(List.of(dummyArticle));

        // When: 수집 스케줄러 실행
        articleCollectorScheduler.collectArticlesByInterest();

        // Then: 관심사 기반 fetch, 저장이 정상 수행됨
        verify(interestRepository).findAll();
        verify(articleFetcher).fetch("인공지능");
        verify(articlesService).saveAll(List.of(dummyArticle));
    }

    @Test
    @DisplayName("뉴스 수집 중 예외가 발생해도 다음 관심사로 계속 진행된다")
    void collectArticles_ShouldContinueOnException() throws Exception {
        // Given: 첫 번째 관심사는 예외 발생, 두 번째는 정상 기사 수집
        Interest aiInterest = createInterestWithId("AI", UUID.randomUUID());
        Interest stockInterest = createInterestWithId("주식", UUID.randomUUID());

        when(interestRepository.findAll()).thenReturn(List.of(aiInterest, stockInterest));

        when(articleFetcher.fetch("AI")).thenThrow(new RuntimeException("Fetch 실패"));
        when(articleFetcher.fetch("주식")).thenReturn(List.of(
            new ArticleSaveRequest(
                stockInterest.getId(),
                "Naver",
                "https://stock.com",
                "주식 뉴스",
                "요약",
                LocalDateTime.now()
            )
        ));

        // When: 수집 스케줄러 실행
        articleCollectorScheduler.collectArticlesByInterest();

        // Then: 첫 관심사는 건너뛰고, 두 번째 관심사 기사만 저장됨
        verify(articlesService).saveAll(any());
    }

    @Test
    @DisplayName("articleFetchers 리스트가 비어있으면 저장 시도하지 않는다")
    void collectArticles_ShouldNotSaveWhenFetchersEmpty() throws Exception {
        // Given: fetcher 리스트가 비어 있음
        ArticleCollectorScheduler scheduler = new ArticleCollectorScheduler(
            interestRepository,
            List.of(), // 빈 fetcher 리스트
            articlesService
        );

        Interest interest = createInterestWithId("테크", UUID.randomUUID());
        when(interestRepository.findAll()).thenReturn(List.of(interest));

        // When: 수집 스케줄러 실행
        scheduler.collectArticlesByInterest();

        // Then: fetch가 수행되지 않으므로 저장도 호출되지 않음
        verifyNoInteractions(articlesService);
    }

    @Test
    @DisplayName("관심사가 없을 경우 수집 작업이 정상 종료된다")
    void collectArticles_ShouldExitGracefully_WhenNoInterests() {
        // Given: 관심사 목록이 비어 있음
        given(interestRepository.findAll()).willReturn(Collections.emptyList());

        // When: 수집 스케줄러 실행
        articleCollectorScheduler.collectArticlesByInterest();

        // Then: 아무런 저장 작업이 수행되지 않음
        verifyNoInteractions(articlesService);
    }

    @Test
    @DisplayName("fetch가 null을 반환하면 저장을 호출하지 않는다")
    void collectArticles_ShouldNotSaveWhenFetcherReturnsNull() throws Exception {
        // Given
        Interest interest = createInterestWithId("테스트", UUID.randomUUID());
        when(interestRepository.findAll()).thenReturn(List.of(interest));
        when(articleFetcher.fetch("테스트")).thenReturn(null);

        // When
        articleCollectorScheduler.collectArticlesByInterest();

        // Then
        verifyNoInteractions(articlesService);
    }

    @Test
    @DisplayName("fetch가 빈 리스트를 반환하면 저장을 호출하지 않는다")
    void collectArticles_ShouldNotSaveWhenFetcherReturnsEmptyList() throws Exception {
        // Given
        Interest interest = createInterestWithId("테스트", UUID.randomUUID());
        when(interestRepository.findAll()).thenReturn(List.of(interest));
        when(articleFetcher.fetch("테스트")).thenReturn(Collections.emptyList());

        // When
        articleCollectorScheduler.collectArticlesByInterest();

        // Then
        verifyNoInteractions(articlesService);
    }

    @Test
    @DisplayName("여러 fetcher가 있을 경우 각각 호출된다")
    void collectArticles_MultipleFetchers_AllCalled() throws Exception {
        // Given
        ArticleFetcher fetcher1 = mock(ArticleFetcher.class);
        ArticleFetcher fetcher2 = mock(ArticleFetcher.class);

        ArticleCollectorScheduler scheduler = new ArticleCollectorScheduler(
            interestRepository,
            List.of(fetcher1, fetcher2),
            articlesService
        );

        Interest interest = createInterestWithId("테크", UUID.randomUUID());
        when(interestRepository.findAll()).thenReturn(List.of(interest));
        when(fetcher1.fetch("테크")).thenReturn(List.of(
            new ArticleSaveRequest(interest.getId(), "Source1", "url1", "title1", "summary1", LocalDateTime.now())
        ));
        when(fetcher2.fetch("테크")).thenReturn(List.of(
            new ArticleSaveRequest(interest.getId(), "Source2", "url2", "title2", "summary2", LocalDateTime.now())
        ));

        // When
        scheduler.collectArticlesByInterest();

        // Then
        verify(fetcher1).fetch("테크");
        verify(fetcher2).fetch("테크");

        verify(articlesService, times(2)).saveAll(any());
    }

    @Test
    @DisplayName("관심사 이름이 빈 문자열일 때 fetch 호출")
    void collectArticles_ShouldCallFetchWithEmptyKeyword() throws Exception {
        // Given
        Interest interest = createInterestWithId("", UUID.randomUUID());
        when(interestRepository.findAll()).thenReturn(List.of(interest));
        when(articleFetcher.fetch("")).thenReturn(Collections.emptyList());

        // When
        articleCollectorScheduler.collectArticlesByInterest();

        // Then
        verify(articleFetcher).fetch("");
    }


    /**
     * ID 필드가 protected이고 BaseEntity에 존재하므로, 리플렉션으로 UUID 값을 강제 주입
     */
    private Interest createInterestWithId(String name, UUID id) throws Exception {
        Interest interest = Interest.builder()
            .name(name)
            .build();

        Field idField = interest.getClass().getSuperclass().getSuperclass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(interest, id);
        return interest;
    }
}
