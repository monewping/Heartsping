package org.project.monewping.domain.article.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.monewping.domain.article.dto.request.ArticleSaveRequest;
import org.project.monewping.domain.article.entity.Interest;
import org.project.monewping.domain.article.repository.ArticlesRepository;

@ExtendWith(MockitoExtension.class)
public class ArticlesServiceTest {

    @InjectMocks
    private NewsArticleServiceImpl articleService;

    @Mock
    private ArticlesRepository articlesRepository;

    @Mock
    private InterestRepository interestRepository;

    @Test
    @DisplayName("중복된 출처 링크가 있으면 DuplicateArticleException 예외 발생")
    void save_ShouldThrowDuplicateException_WhenOriginalLinkExists() {
        UUID interestId = UUID.randomUUID();
        ArticleSaveRequest request = new ArticleSaveRequest(
            interestId, "Naver", "https://naver.com/sample", "제목", "요약", LocalDateTime.now());

        when(articlesRepository.existsByOriginalLink(request.originalLink())).thenReturn(true);

        assertThrows(DuplicateArticleException.class,
            () -> articleService.save(request));
    }

    @Test
    @DisplayName("해당 관심사를 찾을 수 없으면 InterestNotFoundException 예외 발생")
    void save_ShouldThrowException_WhenInterestNotFound() {
        UUID interestId = UUID.randomUUID();
        ArticleSaveRequest request = new ArticleSaveRequest(
            interestId, "Naver", "https://naver.com/sample", "제목", "요약", LocalDateTime.now());

        when(articlesRepository.existsByOriginalLink(request.originalLink())).thenReturn(false);
        when(interestRepository.findById(interestId)).thenReturn(Optional.empty());

        assertThrows(InterestNotFoundException.class,
            () -> articleService.save(request));
    }

    @Test
    @DisplayName("중복된 출처의 뉴스 기사만 제외하고 나머지 뉴스 기사 저장")
    void saveAll_ShouldSaveOnlyNonDuplicateArticles() {
        UUID interestId = UUID.randomUUID();

        Interest interest = Interest.builder()
            .name("AI")
            .subscriberCount(1000)
            .updatedAt(LocalDateTime.now())
            .build();

        ArticleSaveRequest request1 = new ArticleSaveRequest(
            interestId, "Naver", "https://naver.com/sample-1", "제목1", "요약1", LocalDateTime.now());

        ArticleSaveRequest request2 = new ArticleSaveRequest(
            interestId, "Naver", "https://naver.com/sample-2", "제목2", "요약2", LocalDateTime.now());

        ArticleSaveRequest duplicate = new ArticleSaveRequest(
            interestId, "Naver", "https://naver.com/sample-1", "제목3", "요약3", LocalDateTime.now());

        List<ArticleSaveRequest> requests = List.of(request1, request2, duplicate);

        when(interestRepository.findById(interestId)).thenReturn(Optional.of(interest));
        when(articlesRepository.existsByOriginalLink("https://naver.com/sample-1")).thenReturn(true);
        when(articlesRepository.existsByOriginalLink("https://naver.com/sample-2")).thenReturn(false);

        articleService.saveAll(requests);

        verify(articlesRepository, times(1))
            .save(argThat(article -> article.getOriginalLink().equals("https://naver.com/sample-2")));
        verify(articlesRepository, never())
            .save(argThat(article -> article.getOriginalLink().equals("https://naver.com/sample-1")));
    }

}
