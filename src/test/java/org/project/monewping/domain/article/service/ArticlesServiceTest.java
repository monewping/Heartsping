package org.project.monewping.domain.article.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
import org.project.monewping.domain.article.exception.ArticleNotFoundException;
import org.project.monewping.domain.article.mapper.ArticlesMapper;
import org.project.monewping.domain.article.repository.ArticleViewsRepository;
import org.project.monewping.domain.article.repository.ArticlesRepository;
import org.project.monewping.domain.article.service.impl.ArticlesServiceImpl;
import org.project.monewping.domain.notification.repository.NotificationRepository;
import org.project.monewping.global.dto.CursorPageResponse;
import org.project.monewping.domain.interest.entity.Interest;
import org.project.monewping.domain.interest.repository.InterestRepository;

@DisplayName("ArticlesService 테스트")
@ExtendWith(MockitoExtension.class)
public class ArticlesServiceTest {

    @InjectMocks
    private ArticlesServiceImpl articleService;

    @Mock
    private ArticleViewsRepository articleViewsRepository;

    @Mock
    private ArticlesRepository articlesRepository;

    @Mock
    private InterestRepository interestRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private ArticlesMapper articlesMapper;

    @Captor
    private ArgumentCaptor<List<Articles>> articleListCaptor;

    @Test
    @DisplayName("중복된 뉴스 기사 제외하고 나머지를 저장한다")
    void saveAll_ShouldSaveOnlyNonDuplicateArticles() {
        // Given
        UUID interestId = UUID.randomUUID();
        Interest interest = Interest.builder()
            .id(interestId)  // id 필수 세팅
            .name("AI")
            .subscriberCount(1000L)
            .updatedAt(Instant.now())
            .build();

        ArticleSaveRequest request1 = new ArticleSaveRequest(interestId, "Naver", "https://naver.com/sample-1", "제목1", "요약1", LocalDateTime.now());
        ArticleSaveRequest request2 = new ArticleSaveRequest(interestId, "Naver", "https://naver.com/sample-2", "제목2", "요약2", LocalDateTime.now());
        ArticleSaveRequest duplicate = new ArticleSaveRequest(interestId, "Naver", "https://naver.com/sample-1", "제목3", "요약3", LocalDateTime.now());

        List<ArticleSaveRequest> requests = List.of(request1, request2, duplicate);

        when(interestRepository.findById(eq(interestId))).thenReturn(Optional.of(interest));
        when(articlesRepository.findAllByOriginalLinkIn(any())).thenAnswer(invocation -> {
            List<String> links = invocation.getArgument(0);
            // 중복 링크 'https://naver.com/sample-1'이 이미 있다고 가정
            return links.contains("https://naver.com/sample-1")
                ? List.of(Articles.builder().originalLink("https://naver.com/sample-1").build())
                : List.of();
        });

        when(articlesMapper.safeToEntity(any(ArticleSaveRequest.class), eq(interest))).thenAnswer(invocation -> {
            ArticleSaveRequest dto = invocation.getArgument(0);
            return Articles.builder()
                .interest(interest)
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
        verify(articlesRepository).saveAll(articleListCaptor.capture());

        List<Articles> saved = articleListCaptor.getValue();
        assertThat(saved).isNotNull();
        assertThat(saved).allMatch(Objects::nonNull);  // null이 없음을 검증
        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).getOriginalLink()).isEqualTo("https://naver.com/sample-2");

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
        verifyNoInteractions(articlesMapper);
    }

    @Test
    @DisplayName("모든 뉴스 기사가 중복 없이 저장된다")
    void saveAll_ShouldSaveAllWhenNoDuplicates() {
        // Given
        UUID interestId = UUID.randomUUID();
        Interest interest = Interest.builder()
            .id(interestId)  // id 추가
            .name("IT")
            .subscriberCount(500L)
            .updatedAt(Instant.now())
            .build();

        ArticleSaveRequest request1 = new ArticleSaveRequest(interestId, "Naver", "https://naver.com/sample-1", "제목1", "요약1", LocalDateTime.now());
        ArticleSaveRequest request2 = new ArticleSaveRequest(interestId, "Naver", "https://naver.com/sample-2", "제목2", "요약2", LocalDateTime.now());

        when(interestRepository.findById(eq(interestId))).thenReturn(Optional.of(interest));
        when(articlesRepository.findAllByOriginalLinkIn(any())).thenReturn(List.of());  // 중복 없음

        when(articlesMapper.safeToEntity(any(ArticleSaveRequest.class), eq(interest))).thenAnswer(invocation -> {
            ArticleSaveRequest dto = invocation.getArgument(0);
            return Articles.builder()
                .interest(interest)
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
        assertThat(saved).hasSize(2);
        assertThat(saved).extracting(Articles::getOriginalLink)
            .containsExactlyInAnyOrder("https://naver.com/sample-1", "https://naver.com/sample-2");
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
            UUID.randomUUID()  // ✅ 사용자 ID를 실제로 넣어줍니다
        );

        Articles article = Articles.builder()
            .id(UUID.randomUUID())
            .title("인공지능 혁신")
            .source("연합뉴스")
            .originalLink("https://ai.com/1")
            .summary("요약")
            .publishedAt(LocalDateTime.now())
            .viewCount(0L)
            .deleted(false)
            .build();

        given(articlesRepository.searchArticles(request)).willReturn(List.of(article));
        given(articlesRepository.countArticles(request)).willReturn(1L);
        given(articlesMapper.toDto(article)).willReturn(
            new ArticleDto(
                article.getId(),
                article.getSource(),
                article.getOriginalLink(),
                article.getTitle(),
                article.getPublishedAt(),
                article.getSummary(),
                article.getViewCount(),
                0L,
                false
            )
        );

        // ✅ articleViewsRepository 의존성 추가
        given(articleViewsRepository.findAllByViewedByAndArticleIdIn(any(), any()))
            .willReturn(List.of()); // 사용자 조회 없음 처리

        // When
        CursorPageResponse<ArticleDto> result = articleService.findArticles(request);

        // Then
        assertThat(result.content()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.hasNext()).isFalse();
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

    @Test
    @DisplayName("논리 삭제 - 존재하는 기사일 경우 삭제 플래그만 true로 변경 후 저장 및 관련 알림 비활성화")
    void softDelete_Success() {
        // given
        Interest interest = Interest.builder()
                .name("Interest Name")
                .subscriberCount(1L)
                .keywords(Collections.emptyList())
                .build();

        UUID articleId = UUID.randomUUID();
        Articles article = Articles.builder()
            .id(articleId)
            .interest(interest)
            .title("Original Title")
            .summary("Original Summary")
            .originalLink("http://original.link")
            .deleted(false)
            .createdAt(Instant.now())
            .build();

        given(articlesRepository.findByIdAndDeletedFalse(articleId)).willReturn(Optional.of(article));

        doNothing().when(notificationRepository)
            .deactivateByResourceIdAndCreatedAtBetween(
                eq(interest.getId()),
                any(Instant.class),
                any(Instant.class)
            );

        given(notificationRepository.findByResourceIdAndActiveFalseAndCreatedAtBetween(
            eq(interest.getId()),
            any(Instant.class),
            any(Instant.class)
        ))
            .willReturn(Collections.emptyList());

        // when
        articleService.softDelete(articleId);

        // then
        assertThat(article.isDeleted()).isTrue();

        // 제목, 요약, 링크 등은 변경하지 않음
        assertThat(article.getTitle()).isEqualTo("Original Title");
        assertThat(article.getSummary()).isEqualTo("Original Summary");
        assertThat(article.getOriginalLink()).isEqualTo("http://original.link");

        then(notificationRepository).should().deactivateByResourceIdAndCreatedAtBetween(
            eq(interest.getId()),
            any(Instant.class),
            any(Instant.class)
        );
    }

    @Test
    @DisplayName("논리 삭제 - 존재하지 않는 기사일 경우 ArticleNotFoundException 예외 발생")
    void softDelete_NotFound() {
        // given
        UUID articleId = UUID.randomUUID();
        given(articlesRepository.findByIdAndDeletedFalse(articleId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> articleService.softDelete(articleId))
            .isInstanceOf(ArticleNotFoundException.class);
    }

    @Test
    @DisplayName("물리 삭제 - 존재하는 기사일 경우 기사 삭제 수행")
    void hardDelete_Success() {
        // given
        Interest interest = Interest.builder()
            .name("Interest Name")
            .subscriberCount(1L)
            .keywords(Collections.emptyList())
            .build();

        UUID articleId = UUID.randomUUID();
        Articles article = Articles.builder()
            .id(articleId)
            .interest(interest)
            .deleted(false)
            .createdAt(Instant.now())
            .build();

        given(articlesRepository.findById(articleId)).willReturn(Optional.of(article));

        doNothing().when(notificationRepository)
            .deactivateByResourceIdAndCreatedAtBetween(
                eq(interest.getId()),
                any(Instant.class),
                any(Instant.class)
            );
        given(notificationRepository.findByResourceIdAndActiveFalseAndCreatedAtBetween(
            eq(interest.getId()),
            any(Instant.class),
            any(Instant.class)
        ))
            .willReturn(Collections.emptyList());

        // when
        articleService.hardDelete(articleId);

        // then
        verify(articlesRepository).delete(article);

        then(notificationRepository).should().deactivateByResourceIdAndCreatedAtBetween(
            eq(interest.getId()),
            any(Instant.class),
            any(Instant.class)
        );
    }

    @Test
    @DisplayName("물리 삭제 - 존재하지 않는 기사일 경우 ArticleNotFoundException 예외 발생")
    void hardDelete_NotFound() {
        // given
        UUID articleId = UUID.randomUUID();
        given(articlesRepository.findById(articleId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> articleService.hardDelete(articleId))
            .isInstanceOf(ArticleNotFoundException.class);
    }
}
