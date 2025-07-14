package org.project.monewping.domain.article.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.monewping.domain.article.dto.data.ArticleViewDto;
import org.project.monewping.domain.article.entity.NewsViewHistory;
import org.project.monewping.domain.article.exception.DuplicateViewHistoryException;
import org.project.monewping.domain.article.mapper.NewsViewHistoryMapper;
import org.project.monewping.domain.article.repository.NewsViewHistoryRepository;

@ExtendWith(MockitoExtension.class)
public class ArticleServiceTest {

    @InjectMocks
    private ArticleViewServiceImpl articleViewService;

    @Mock
    private NewsViewHistoryRepository viewHistoryRepository;

    @Mock
    private NewsViewHistoryMapper mapper;

    private UUID viewedBy;
    private UUID articleId;
    private ArticleViewDto dto;

    @BeforeEach
    void setUp() {
        viewedBy = UUID.randomUUID();
        articleId = UUID.randomUUID();
        dto = new ArticleViewDto(UUID.randomUUID(), viewedBy, articleId, LocalDateTime.now());
    }

    @Test
    @DisplayName("사용자가 기사를 처음 조회하면 조회 기록이 저장된다")
    void registerView_Success() {
        // given
        given(viewHistoryRepository.findByViewedByAndArticleId(viewedBy, articleId))
            .willReturn(Optional.empty());
        NewsViewHistory history = new NewsViewHistory(UUID.randomUUID(), viewedBy, articleId, dto.articlePublishedDate());
        given(mapper.toEntity(dto)).willReturn(history);

        // when
        articleViewService.registerView(dto);

        // then
        then(viewHistoryRepository).should().save(history);
    }

    @Test
    @DisplayName("사용자가 이미 조회한 기사 중복 조회 시 예외 발생")
    void registerView_ShouldThrowException_WhenDuplicateView() {
        // given
        given(viewHistoryRepository.findByViewedByAndArticleId(viewedBy, articleId))
            .willReturn(Optional.of(mock(NewsViewHistory.class)));

        // when & then
        assertThatThrownBy(() -> articleViewService.registerView(dto))
            .isInstanceOf(DuplicateViewHistoryException.class)
            .hasMessageContaining("이미 조회한 기사");
    }

}
