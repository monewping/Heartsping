package org.project.monewping.domain.article.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.monewping.domain.article.dto.data.ArticleDto;
import org.project.monewping.domain.article.dto.response.ArticleRestoreResultDto;
import org.project.monewping.domain.article.entity.Articles;
import org.project.monewping.domain.article.exception.InvalidRestoreRangeException;
import org.project.monewping.domain.article.mapper.ArticlesMapper;
import org.project.monewping.domain.article.repository.ArticlesRepository;
import org.project.monewping.domain.article.service.impl.ArticleRestoreServiceImpl;
import org.project.monewping.domain.article.storage.ArticleBackupStorage;

@ExtendWith(MockitoExtension.class)
@DisplayName("ArticleRestoreService 테스트")
public class ArticleRestoreServiceTest {

    @Mock
    private ArticleBackupStorage backupStorage;

    @Mock
    private ArticlesRepository articlesRepository;

    @Mock
    private ArticlesMapper articlesMapper;

    @InjectMocks
    private ArticleRestoreServiceImpl restoreService;

    @Test
    @DisplayName("복구 시 시작 날짜가 끝 날짜보다 늦으면 예외가 발생한다")
    void restoreArticlesByRange_invalidDateRange_throwsException() {
        // given
        LocalDateTime from = LocalDateTime.of(2025, 7, 20, 0, 0);
        LocalDateTime to = LocalDateTime.of(2025, 7, 19, 0, 0);

        // when & then
        assertThrows(InvalidRestoreRangeException.class, () -> {
            restoreService.restoreArticlesByRange(from, to);
        });
    }

    @Test
    @DisplayName("백업 데이터가 없으면 빈 결과를 반환한다")
    void restoreArticlesByRange_emptyBackupData_returnsEmptyResult() {
        // given
        LocalDate date = LocalDate.of(2025, 7, 18);
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = from;

        when(backupStorage.load(date)).thenReturn(Collections.emptyList());

        // when
        List<ArticleRestoreResultDto> result = restoreService.restoreArticlesByRange(from, to);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(from, result.get(0).restoreDate());
        assertTrue(result.get(0).restoredArticleIds().isEmpty());
        assertEquals(0, result.get(0).restoredArticleCount());
    }

    @Test
    @DisplayName("백업 데이터가 있을 때 기존에 없는 기사만 복구한다")
    void restoreArticlesByRange_backupData_restoresOnlyMissingArticles() {
        // given
        LocalDate date = LocalDate.of(2025, 7, 18);
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = from;

        ArticleDto article1 = new ArticleDto(UUID.randomUUID(), "source1", "url1", "title1",
            LocalDateTime.now(), "summary", 0L, 0L, false);
        ArticleDto article2 = new ArticleDto(UUID.randomUUID(), "source2", "url2", "title2",
            LocalDateTime.now(), "summary", 0L, 0L, false);
        List<ArticleDto> backupList = List.of(article1, article2);

        when(backupStorage.load(date)).thenReturn(backupList);
        when(articlesRepository.findExistingOriginalLinks(List.of("url1", "url2"))).thenReturn(List.of("url1"));

        Articles entity2 = mock(Articles.class);
        when(articlesMapper.toEntity(article2)).thenReturn(entity2);
        UUID savedId = UUID.randomUUID();
        when(entity2.getId()).thenReturn(savedId);
        when(articlesRepository.saveAll(anyList())).thenReturn(List.of(entity2));

        // when
        List<ArticleRestoreResultDto> result = restoreService.restoreArticlesByRange(from, to);

        // then
        assertEquals(1, result.size());
        ArticleRestoreResultDto dto = result.get(0);
        assertEquals(from, dto.restoreDate());
        assertEquals(1, dto.restoredArticleCount());
        assertEquals(List.of(savedId.toString()), dto.restoredArticleIds());
    }
}