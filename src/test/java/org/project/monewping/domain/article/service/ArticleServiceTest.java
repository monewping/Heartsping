package org.project.monewping.domain.article.service;

import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ArticleServiceTest {

    @InjectMocks
    private ArticleViewService viewService;

    @Mock
    private NewsViewHistoryRepository viewHistoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NewsArticleRepository articleRepository;

    @Mock
    private NewsViewHistoryMapper mapper;

    private UUID userId;
    private UUID articleId;
    private ArticleViewDto dto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        articleId = UUID.randomUUID();
        dto = new ArticleViewDto(userId, articleId, LocalDateTime.now());
    }

    @Test
    @DisplayName("기사 뷰 정상 등록")
    void registerView_Success() {
        // given
        User user = new User(userId, "test@example.com");
        NewsArticle article = new NewsArticle(articleId, "title", "link");

        given(viewHistoryRepository.findByUserUserIdAndArticleArticleId(userId, articleId))
            .willReturn(Optional.empty());
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(articleRepository.findById(articleId)).willReturn(Optional.of(article));

        NewsViewHistory history = new NewsViewHistory(user, article, dto.ViewdAt());
        given(mapper.toEntity(dto, user, article)).willReturn(history);

        // when
        articleViewService.registerView(dto);

        // then
        then(viewHistoryRepository).should().save(history);
    }

    @Test
    @DisplayName("기사 중복 조회 시 예외 발생")
    void registerView_ShouldThrowException_WhenDuplicateView() {
        // given
        given(viewHistoryRepository.findByUserIdAndArticleId(userId, articleId))
            .willReturn(Optional.of(mock(NewsViewHistory.class)));

        // when & then
        assertThatThrownBy(() -> articleViewService.registerView(dto))
            .isInstanceOf(DuplicateViewHistoryException.class)
            .hasMessageContaining("이미 조회한 기사");
    }

}
