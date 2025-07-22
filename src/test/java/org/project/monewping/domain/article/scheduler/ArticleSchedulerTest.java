package org.project.monewping.domain.article.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.project.monewping.domain.article.dto.request.ArticleSaveRequest;
import org.project.monewping.domain.article.fetcher.ArticleFetcher;
import org.project.monewping.domain.article.service.ArticlesService;
import org.project.monewping.domain.interest.entity.Interest;
import org.project.monewping.domain.interest.repository.InterestRepository;
import org.project.monewping.domain.interest.repository.KeywordRepository;

@DisplayName("ArticleScheduler 테스트")
public class ArticleSchedulerTest {

    @Mock
    private InterestRepository interestRepository;

    @Mock
    private KeywordRepository keywordRepository;

    @Mock
    private ArticlesService articlesService;

    @Mock
    private ArticleFetcher fetcher1;

    @InjectMocks
    private ArticleCollectorScheduler articleCollectorScheduler;

    private final UUID interestId = UUID.randomUUID();
    private final Interest interest = Interest.builder().id(interestId).name("경제").build();
    private final String keyword = "금리";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        articleCollectorScheduler = new ArticleCollectorScheduler(
            interestRepository,
            List.of(fetcher1),   // fetcher2 제거, 단일 fetcher만 주입
            articlesService,
            keywordRepository
        );
    }

    @Test
    @DisplayName("관심사 기반 뉴스 기사 수집 성공 테스트")
    void testCollectArticlesByInterest() {
        // given
        List<Interest> interests = List.of(interest);
        List<String> keywords = List.of(keyword);
        List<ArticleSaveRequest> dummyArticles = List.of(
            new ArticleSaveRequest(interestId, "연합뉴스", "https://test", "금리 인상", "설명", null)
        );

        when(interestRepository.findAll()).thenReturn(interests);
        when(keywordRepository.findNamesByInterestId(interestId)).thenReturn(keywords);
        when(fetcher1.fetch(eq(interestId), eq(List.of(keyword)))).thenReturn(dummyArticles);

        // when
        articleCollectorScheduler.collectArticlesByInterest();

        // then
        verify(articlesService, times(1)).saveAll(dummyArticles);
        verify(fetcher1, times(1)).fetch(eq(interestId), eq(List.of(keyword)));
        verify(keywordRepository, times(1)).findNamesByInterestId(interestId);
        verify(interestRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("수집 도중 예외 발생 시 로깅하고 계속 진행")
    void testCollectArticlesWithFetcherException() {
        // given
        List<Interest> interests = List.of(interest);
        List<String> keywords = List.of(keyword);

        when(interestRepository.findAll()).thenReturn(interests);
        when(keywordRepository.findNamesByInterestId(interestId)).thenReturn(keywords);
        when(fetcher1.fetch(eq(interestId), eq(List.of(keyword))))
            .thenThrow(new RuntimeException("API error"));

        // when
        articleCollectorScheduler.collectArticlesByInterest();

        // then
        verify(fetcher1, times(1)).fetch(eq(interestId), eq(List.of(keyword)));
        verify(articlesService, never()).saveAll(any());
    }
}
