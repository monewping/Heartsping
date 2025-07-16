package org.project.monewping.domain.article.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.monewping.domain.article.dto.request.ArticleSaveRequest;
import org.project.monewping.domain.article.entity.Interest;
import org.project.monewping.domain.article.fetcher.ArticleFetcher;
import org.project.monewping.domain.article.repository.InterestRepository;
import org.project.monewping.domain.article.scheduler.ArticleCollectorScheduler;

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
        // given
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

        // when
        articleCollectorScheduler.collectArticlesByInterest();

        // then
        verify(interestRepository).findAll();
        verify(articleFetcher).fetch("인공지능");
        verify(articlesService).saveAll(List.of(dummyArticle));
    }

    /**
     * ID 필드가 protected이고 BaseEntity에 존재하므로, 리플렉션으로 UUID 값을 강제 주입
     */
    private Interest createInterestWithId(String name, UUID id) throws Exception {
        Interest interest = Interest.builder()
            .name(name)
            .build();

        Field idField = interest.getClass().getSuperclass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(interest, id);
        return interest;
    }

}
