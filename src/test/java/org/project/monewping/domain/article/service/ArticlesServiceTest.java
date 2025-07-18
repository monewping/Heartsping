package org.project.monewping.domain.article.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.monewping.domain.article.dto.data.ArticleDto;
import org.project.monewping.domain.article.dto.request.ArticleSaveRequest;
import org.project.monewping.domain.article.dto.request.ArticleSearchRequest;
import org.project.monewping.domain.article.entity.Articles;
import org.project.monewping.domain.article.exception.DuplicateArticleException;
import org.project.monewping.domain.article.exception.InterestNotFoundException;
import org.project.monewping.domain.article.mapper.ArticlesMapper;
import org.project.monewping.domain.article.repository.ArticlesRepository;
import org.project.monewping.domain.article.repository.InterestRepository;
import org.project.monewping.global.dto.CursorPageResponse;
import org.project.monewping.domain.interest.entity.Interest;
import org.project.monewping.domain.interest.repository.InterestRepository;

@DisplayName("ArticlesService 테스트")
@ExtendWith(MockitoExtension.class)
public class ArticlesServiceTest {

    @InjectMocks
    private ArticlesServiceImpl articleService;

    @Mock
    private ArticlesRepository articlesRepository;

    @Mock
    private InterestRepository interestRepository;

    @Mock
    private ArticlesMapper articlesMapper;

    @Captor
    ArgumentCaptor<Articles> articleCaptor;

    @Test
    @DisplayName("originalLink가 이미 존재하면 DuplicateArticleException 예외 발생")
    void save_ShouldThrowDuplicateException_WhenOriginalLinkExists() {
        // Given
        UUID interestId = UUID.randomUUID();
        ArticleSaveRequest request = new ArticleSaveRequest(
            interestId, "Naver", "https://naver.com/sample", "제목", "요약", LocalDateTime.now()
        );
        when(articlesRepository.existsByOriginalLink(request.originalLink())).thenReturn(true);

        // When & Then
        assertThrows(DuplicateArticleException.class,
            () -> articleService.save(request));
    }

    @Test
    @DisplayName("존재하지 않는 관심사일 경우 InterestNotFoundException 예외 발생")
    void save_ShouldThrowException_WhenInterestNotFound() {
        // Given
        UUID interestId = UUID.randomUUID();
        ArticleSaveRequest request = new ArticleSaveRequest(
            interestId, "Naver", "https://naver.com/sample", "제목", "요약", LocalDateTime.now()
        );

        when(articlesRepository.existsByOriginalLink(request.originalLink())).thenReturn(false);
        when(interestRepository.findById(interestId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(InterestNotFoundException.class,
            () -> articleService.save(request));
    }

    @Test
    @DisplayName("중복된 뉴스 기사 제외하고 나머지를 저장한다")
    void saveAll_ShouldSaveOnlyNonDuplicateArticles() {
        // Given
        UUID interestId = UUID.randomUUID();
        Interest interest = Interest.builder()
            .name("AI")
            .subscriberCount(1000L)
            .updatedAt(Instant.now())
            .build();

        ArticleSaveRequest request1 = new ArticleSaveRequest(interestId, "Naver", "https://naver.com/sample-1", "제목1", "요약1", LocalDateTime.now());
        ArticleSaveRequest request2 = new ArticleSaveRequest(interestId, "Naver", "https://naver.com/sample-2", "제목2", "요약2", LocalDateTime.now());
        ArticleSaveRequest duplicate = new ArticleSaveRequest(interestId, "Naver", "https://naver.com/sample-1", "제목3", "요약3", LocalDateTime.now());

        List<ArticleSaveRequest> requests = List.of(request1, request2, duplicate);

        when(interestRepository.findById(interestId)).thenReturn(Optional.of(interest));
        when(articlesRepository.findAllByOriginalLinkIn(any())).thenReturn(
            List.of(Articles.builder().originalLink("https://naver.com/sample-1").build())
        );

        when(articlesMapper.toEntity(any(ArticleSaveRequest.class), any(Interest.class)))
            .thenAnswer(invocation -> {
                ArticleSaveRequest dto = invocation.getArgument(0);
                Interest intr = invocation.getArgument(1);
                return Articles.builder()
                    .interest(intr)
                    .source(dto.source())
                    .originalLink(dto.originalLink())
                    .title(dto.title())
                    .summary(dto.summary())
                    .publishedAt(dto.publishedAt())
                    .viewCount(0)
                    .deleted(false)
                    .build();
            });

        // When
        articleService.saveAll(requests);

        // Then
        ArgumentCaptor<List<Articles>> captor = ArgumentCaptor.forClass(List.class);
        verify(articlesRepository).saveAll(captor.capture());

        List<Articles> saved = captor.getValue();
        assertEquals(1, saved.size());
        assertEquals("https://naver.com/sample-2", saved.get(0).getOriginalLink());
        assertFalse(saved.stream().anyMatch(a -> a.getOriginalLink().equals("https://naver.com/sample-1")));
    }

    @Test
    @DisplayName("유효한 요청 시 뉴스 기사가 저장된다")
    void save_ShouldPersistArticle_WhenValid() {
        // Given
        UUID interestId = UUID.randomUUID();
        ArticleSaveRequest request = new ArticleSaveRequest(interestId, "Naver", "https://naver.com/sample", "제목", "요약", LocalDateTime.now());

        Interest interest = Interest.builder()
            .name("AI")
            .subscriberCount(1000L)
            .updatedAt(Instant.now())
            .build();

        Articles expectedArticle = Articles.builder()
            .interest(interest)
            .source(request.source())
            .originalLink(request.originalLink())
            .title(request.title())
            .summary(request.summary())
            .publishedAt(request.publishedAt())
            .viewCount(0)
            .deleted(false)
            .build();

        when(articlesRepository.existsByOriginalLink(request.originalLink())).thenReturn(false);
        when(interestRepository.findById(interestId)).thenReturn(Optional.of(interest));
        when(articlesMapper.toEntity(request, interest)).thenReturn(expectedArticle);

        // When
        articleService.save(request);

        // Then
        verify(articlesRepository).save(expectedArticle);
    }

    @Test
    @DisplayName("빈 리스트 저장 요청 시 저장을 시도하지 않는다")
    void saveAll_ShouldHandleEmptyListGracefully() {
        // Given
        List<ArticleSaveRequest> emptyList = List.of();

        // When
        articleService.saveAll(emptyList);

        // Then
        verifyNoInteractions(articlesRepository);
        verifyNoInteractions(interestRepository);
    }

    @Test
    @DisplayName("중복 없이 모든 뉴스 기사가 저장된다")
    void saveAll_ShouldSaveAllWhenNoDuplicates() {
        // Given
        UUID interestId = UUID.randomUUID();
        Interest interest = Interest.builder()
            .name("IT")
            .subscriberCount(500L)
            .updatedAt(Instant.now())
            .build();

        ArticleSaveRequest request1 = new ArticleSaveRequest(interestId, "Naver", "https://naver.com/sample-1", "제목1", "요약1", LocalDateTime.now());
        ArticleSaveRequest request2 = new ArticleSaveRequest(interestId, "Naver", "https://naver.com/sample-2", "제목2", "요약2", LocalDateTime.now());

        when(interestRepository.findById(interestId)).thenReturn(Optional.of(interest));
        when(articlesRepository.findAllByOriginalLinkIn(any())).thenReturn(List.of());

        when(articlesMapper.toEntity(any(ArticleSaveRequest.class), any(Interest.class)))
            .thenAnswer(invocation -> {
                ArticleSaveRequest dto = invocation.getArgument(0);
                Interest intr = invocation.getArgument(1);
                return Articles.builder()
                    .interest(intr)
                    .source(dto.source())
                    .originalLink(dto.originalLink())
                    .title(dto.title())
                    .summary(dto.summary())
                    .publishedAt(dto.publishedAt())
                    .viewCount(0)
                    .deleted(false)
                    .build();
            });

        // When
        articleService.saveAll(List.of(request1, request2));

        // Then
        ArgumentCaptor<List<Articles>> captor = ArgumentCaptor.forClass(List.class);
        verify(articlesRepository).saveAll(captor.capture());

        List<Articles> saved = captor.getValue();
        assertEquals(2, saved.size());
    }

    @Test
    @DisplayName("originalLink가 null이면 IllegalArgumentException 예외 발생")
    void save_ShouldThrowException_WhenOriginalLinkIsNull() {
        // Given
        ArticleSaveRequest request = new ArticleSaveRequest(
            UUID.randomUUID(), "Naver", null, "제목", "요약", LocalDateTime.now()
        );

        // When & Then
        assertThatThrownBy(() -> articleService.save(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("originalLink는 필수입니다");
    }

    @Test
    @DisplayName("유효한 요청이면 뉴스 기사를 저장한다 (통합)")
    void save_ShouldSaveArticle_WhenValidRequest() {
        // Given
        UUID interestId = UUID.randomUUID();
        Interest interest = Interest.builder()
            .id(interestId)
            .name("IT")
            .subscriberCount(100L)
            .updatedAt(Instant.now())
            .build();

        ArticleSaveRequest request = new ArticleSaveRequest(
            interestId, "조선일보", "https://news.com", "제목", "요약", LocalDateTime.now()
        );

        Articles article = Articles.builder()
            .id(UUID.randomUUID())
            .title(request.title())
            .source(request.source())
            .originalLink(request.originalLink())
            .publishedAt(request.publishedAt())
            .summary(request.summary())
            .interest(interest)
            .viewCount(0L)
            .commentCount(0L)
            .deleted(false)
            .build();

        given(interestRepository.findById(interestId)).willReturn(Optional.of(interest));
        given(articlesRepository.existsByOriginalLink(request.originalLink())).willReturn(false);
        given(articlesMapper.toEntity(request, interest)).willReturn(article);

        // When
        articleService.save(request);

        // Then
        verify(articlesRepository).save(article);
    }

    @Test
    @DisplayName("검색어로 제목 또는 요약에 일치하는 기사들을 조회할 수 있다")
    void findArticles_ByKeywordOnly() {
        // Given
        ArticleSearchRequest request = new ArticleSearchRequest(
            "인공지능",
            null,
            null,
            null,
            null,
            "publishDate",
            "DESC",
            null,
            null,
            10,
            null
        );

        given(articlesRepository.searchArticles(request)).willReturn(List.of());

        // When
        CursorPageResponse<ArticleDto> result = articleService.findArticles(request);

        // Then
        assertThat(result.content()).isEmpty();
        assertThat(result.hasNext()).isFalse();
        assertThat(result.totalElements()).isZero();
    }

    @Test
    @DisplayName("관심사, 출처, 날짜 범위 필터와 함께 기사 목록을 조회할 수 있다")
    void findArticles_WithAllFilters() {
        // Given
        UUID interestId = UUID.randomUUID();
        List<String> sources = List.of("연합뉴스");
        LocalDateTime fromDate = LocalDateTime.now().minusDays(7);
        LocalDateTime toDate = LocalDateTime.now();

        ArticleSearchRequest request = new ArticleSearchRequest(
            "AI",
            interestId,
            sources,
            fromDate,
            toDate,
            "viewCount",
            "DESC",
            null,
            null,
            2,
            null
        );

        Articles article = Articles.builder()
            .id(UUID.randomUUID())
            .source("연합뉴스")
            .originalLink("https://news.com/article1")
            .title("AI 산업 동향")
            .summary("요약")
            .publishedAt(LocalDateTime.now().minusDays(1))
            .build();

        given(articlesRepository.searchArticles(request)).willReturn(List.of(article));
        given(articlesRepository.countArticles(request)).willReturn(1L); // ✅ 추가된 부분
        given(articlesMapper.toDto(article)).willReturn(new ArticleDto(
            article.getId(),
            article.getSource(),
            article.getOriginalLink(),
            article.getTitle(),
            article.getPublishedAt(),
            article.getSummary(),
            0L,
            0L,
            false
        ));

        // When
        CursorPageResponse<ArticleDto> result = articleService.findArticles(request);

        // Then
        assertThat(result.content()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1);
    }


    @Test
    @DisplayName("정렬 기준에 따라 다른 방식으로 커서 페이징을 수행할 수 있다")
    void findArticles_SortedByCommentCount_WithCursor() {
        // Given
        UUID cursorId = UUID.randomUUID();

        ArticleSearchRequest request = new ArticleSearchRequest(
            null,
            null,
            null,
            null,
            null,
            "commentCount",
            "DESC",
            cursorId.toString(),
            null,
            5,
            null
        );

        given(articlesRepository.searchArticles(request)).willReturn(List.of());

        // When
        CursorPageResponse<ArticleDto> result = articleService.findArticles(request);

        // Then
        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isZero();
    }

    @Test
    @DisplayName("출처 목록 조회 - 삭제된 기사 제외하고 중복 없이 반환")
    void getAllSources_ReturnsUniqueNonDeletedSources() {
        // given - repository가 중복, 삭제된 출처 포함 리스트 반환
        List<String> mockSources = Arrays.asList("NAVER", "중앙일보", "NAVER", "한겨레");
        when(articlesRepository.findDistinctSources()).thenReturn(mockSources);

        // when
        List<String> result = articleService.getAllSources();

        // then - 중복 제거 로직이 서비스에 있으면 검증
        assertThat(result).containsExactlyInAnyOrder("NAVER", "중앙일보", "한겨레");
    }

    @Test
    @DisplayName("출처 목록 조회 - 결과가 없으면 빈 리스트 반환")
    void getAllSources_ReturnsEmptyListWhenNoSources() {
        when(articlesRepository.findDistinctSources()).thenReturn(Collections.emptyList());

        List<String> result = articleService.getAllSources();

        assertThat(result).isEmpty();
    }
}
