package org.project.monewping.domain.article.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.monewping.domain.article.dto.data.ArticleViewDto;
import org.project.monewping.domain.article.entity.ArticleViews;
import org.project.monewping.domain.article.entity.Articles;
import org.project.monewping.domain.article.exception.ArticleNotFoundException;
import org.project.monewping.domain.article.exception.DuplicateArticleViewsException;
import org.project.monewping.domain.article.repository.ArticleViewsRepository;
import org.project.monewping.domain.article.repository.ArticlesRepository;
import org.project.monewping.domain.interest.entity.Interest;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("ArticleViewsService 테스트")
@ExtendWith(MockitoExtension.class)
public class ArticleViewsServiceTest {

    @InjectMocks
    private ArticleViewsServiceImpl articleViewsService;

    @Mock
    private ArticleViewsRepository articleViewsRepository;

    @Mock
    private ArticlesRepository articlesRepository;

    private UUID viewedBy;
    private UUID articleId;
    private Articles articles;

    @BeforeEach
    void setUp() {
        viewedBy = UUID.randomUUID();
        articleId = UUID.randomUUID();
        articles = Articles.builder()
            .interest(Interest.builder()
                .name("경제")
                .subscriberCount(100L)
                .updatedAt(Instant.now())
                .build())
            .source("네이버")
            .originalLink("https://news.naver.com/sample")
            .title("테스트 기사")
            .summary("기사 요약입니다.")
            .publishedAt(LocalDateTime.of(2025, 7, 15, 12, 0))
            .commentCount(5L)
            .viewCount(123L)
            .deleted(false)
            .build();

        ReflectionTestUtils.setField(articles, "id", articleId);
    }

    @Test
    @DisplayName("사용자가 기사를 처음 조회하면 조회 기록이 저장된다")
    void registerView_success() {
        // given - 기존에 동일한 조회 기록이 없고, 해당 기사도 존재하는 경우
        given(articleViewsRepository.findByViewedByAndArticleId(viewedBy, articleId))
            .willReturn(Optional.empty());
        given(articlesRepository.findById(articleId))
            .willReturn(Optional.of(articles));
        given(articleViewsRepository.save(any(ArticleViews.class)))
            .willAnswer(invocation -> invocation.getArgument(0));

        // when - 조회 기록 등록
        ArticleViewDto result = articleViewsService.registerView(viewedBy, articleId);

        // then - 저장된 Entity와 반환된 Dto 검증
        ArgumentCaptor<ArticleViews> captor = ArgumentCaptor.forClass(ArticleViews.class);
        then(articleViewsRepository).should().save(captor.capture());

        ArticleViews savedEntity = captor.getValue();

        assertThat(result).isNotNull();
        assertThat(result.articleId()).isEqualTo(articleId);
        assertThat(result.viewedBy()).isEqualTo(viewedBy);
        assertThat(result.articleTitle()).isEqualTo(articles.getTitle());
        assertThat(result.articleCommentCount()).isEqualTo(articles.getCommentCount());
        assertThat(result.articleViewCount()).isEqualTo(articles.getViewCount());

        assertThat(savedEntity.getArticle()).isEqualTo(articles);
        assertThat(savedEntity.getViewedBy()).isEqualTo(viewedBy);
        assertThat(savedEntity.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("사용자가 이미 조회한 기사에 대해 중복 등록하면 예외 발생")
    void registerView_duplicate_shouldThrow() {
        // given - 이미 조회 기록이 존재하는 경우
        ArticleViews existingView = ArticleViews.builder()
            .id(UUID.randomUUID())
            .viewedBy(viewedBy)
            .article(articles)
            .createdAt(LocalDateTime.now())
            .build();

        given(articleViewsRepository.findByViewedByAndArticleId(viewedBy, articleId))
            .willReturn(Optional.of(existingView));

        // when & then - 예외 발생 확인
        assertThatThrownBy(() -> articleViewsService.registerView(viewedBy, articleId))
            .isInstanceOf(DuplicateArticleViewsException.class)
            .hasMessageContaining("이미 조회한 기사");
    }

    @Test
    @DisplayName("조회 기록 저장 시 해당 기사가 존재하지 않으면 예외 발생")
    void registerView_shouldThrowException_whenArticleNotFound() {
        // given
        given(articleViewsRepository.findByViewedByAndArticleId(viewedBy, articleId))
            .willReturn(Optional.empty());
        given(articlesRepository.findById(articleId))
            .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> articleViewsService.registerView(viewedBy, articleId))
            .isInstanceOf(ArticleNotFoundException.class)
            .hasMessageContaining("해당 뉴스 기사를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("정상적인 기사 조회 시 기록이 저장된다")
    void registerView_ShouldSaveView_WhenValidRequest() {
        // given
        UUID userId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();

        Articles article = Articles.builder()
            .id(articleId)
            .title("뉴스")
            .originalLink("https://news.com")
            .publishedAt(LocalDateTime.now())
            .viewCount(0L)
            .deleted(false)
            .build();

        given(articlesRepository.findById(articleId)).willReturn(Optional.of(article));
        lenient().when(articleViewsRepository.existsByViewedByAndArticleId(userId, articleId))
            .thenReturn(false);
        given(articleViewsRepository.save(any(ArticleViews.class)))
            .willAnswer(invocation -> invocation.getArgument(0));

        // when
        ArticleViewDto result = articleViewsService.registerView(userId, articleId);

        // then
        verify(articleViewsRepository).save(any(ArticleViews.class));
        assertThat(result).isNotNull();
        assertThat(result.articleId()).isEqualTo(articleId);
        assertThat(result.viewedBy()).isEqualTo(userId);
    }
}
