package org.project.monewping.domain.article.service;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.monewping.domain.article.dto.data.ArticleDto;
import org.project.monewping.domain.article.entity.Articles;
import org.project.monewping.domain.article.mapper.ArticlesMapper;
import org.project.monewping.domain.article.repository.ArticlesRepository;
import org.project.monewping.domain.article.service.impl.ArticleBackupServiceImpl;
import org.project.monewping.domain.article.storage.ArticleBackupStorage;
import org.project.monewping.domain.interest.entity.Interest;

@ExtendWith(MockitoExtension.class)
@DisplayName("ArticleBackupService 테스트")
public class ArticleBackupServiceTest {

    @Mock
    private ArticlesRepository articlesRepository;

    @Mock
    private ArticleBackupStorage backupStorage;

    @Mock
    private ArticlesMapper articlesMapper;

    @InjectMocks
    private ArticleBackupServiceImpl backupService;

    @Test
    @DisplayName("지정한 날짜의 기사들을 백업 스토리지에 저장한다")
    void backupArticlesByDate_savesArticles() {
        // given
        LocalDate date = LocalDate.of(2025, 7, 18);
        UUID articleId = UUID.randomUUID();

        Interest interest = Interest.builder()
            .name("AI")
            .subscriberCount(100L)
            .updatedAt(Instant.now())
            .build();

        Articles article = Articles.builder()
            .id(articleId)
            .interest(interest)  // <-- 변경된 부분
            .source("source")
            .originalLink("url")
            .title("title")
            .summary("summary")
            .publishedAt(LocalDateTime.now())
            .commentCount(0L)
            .viewCount(0L)
            .deleted(false)
            .build();

        when(articlesRepository.findByPublishedAtBetweenAndDeletedFalse(
            date.atStartOfDay(), date.plusDays(1).atStartOfDay()))
            .thenReturn(List.of(article));

        ArticleDto dto = new ArticleDto(articleId, "source", "url", "title",
            LocalDateTime.now(), "summary", 0L, 0L, false);

        when(articlesMapper.toDto(article)).thenReturn(dto);

        // when
        backupService.backupArticlesByDate(date);

        // then
        verify(backupStorage).save(eq(date), argThat(list ->
            list.size() == 1 && list.get(0).sourceUrl().equals("url")));
    }

}
