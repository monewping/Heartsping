package org.project.monewping.domain.article.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
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
import org.project.monewping.domain.article.dto.request.ArticleSaveRequest;
import org.project.monewping.domain.article.entity.Articles;
import org.project.monewping.domain.article.entity.Interest;
import org.project.monewping.domain.article.exception.DuplicateArticleException;
import org.project.monewping.domain.article.exception.InterestNotFoundException;
import org.project.monewping.domain.article.mapper.ArticlesMapper;
import org.project.monewping.domain.article.repository.ArticlesRepository;
import org.project.monewping.domain.article.repository.InterestRepository;

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
            .subscriberCount(1000)
            .updatedAt(LocalDateTime.now())
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
            .subscriberCount(1000)
            .updatedAt(LocalDateTime.now())
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
            .subscriberCount(500)
            .updatedAt(LocalDateTime.now())
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
            .subscriberCount(100)
            .updatedAt(LocalDateTime.now())
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
}
