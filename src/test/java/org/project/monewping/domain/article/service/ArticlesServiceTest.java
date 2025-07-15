package org.project.monewping.domain.article.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
    @DisplayName("중복된 출처 링크가 있으면 DuplicateArticleException 예외 발생")
    void save_ShouldThrowDuplicateException_WhenOriginalLinkExists() {
        // given - 저장하려는 원본링크가 이미 존재함
        UUID interestId = UUID.randomUUID();
        ArticleSaveRequest request = new ArticleSaveRequest(
            interestId, "Naver", "https://naver.com/sample", "제목", "요약", LocalDateTime.now());

        when(articlesRepository.existsByOriginalLink(request.originalLink())).thenReturn(true);

        // when & then - 예외 발생 확인
        assertThrows(DuplicateArticleException.class,
            () -> articleService.save(request));
    }

    @Test
    @DisplayName("해당 관심사를 찾을 수 없으면 InterestNotFoundException 예외 발생")
    void save_ShouldThrowException_WhenInterestNotFound() {
        // given - 유효한 관심사 ID가 없고, 원본 링크는 중복되지 않음
        UUID interestId = UUID.randomUUID();
        ArticleSaveRequest request = new ArticleSaveRequest(
            interestId, "Naver", "https://naver.com/sample", "제목", "요약", LocalDateTime.now());

        when(articlesRepository.existsByOriginalLink(request.originalLink())).thenReturn(false);
        when(interestRepository.findById(interestId)).thenReturn(Optional.empty());

        // when & then - 예외 발생 확인
        assertThrows(InterestNotFoundException.class,
            () -> articleService.save(request));
    }

    @Test
    @DisplayName("중복된 출처의 뉴스 기사만 제외하고 나머지 뉴스 기사 저장")
    void saveAll_ShouldSaveOnlyNonDuplicateArticles() {
        // given - 관심사, 기사 요청 목록, 중복 여부 설정
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

        // articlesMapper.toEntity() 모킹해서 실제 엔티티 리턴하도록 설정
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

        // when - 기사 리스트 저장 요청
        articleService.saveAll(requests);

        // then - 중복되지 않은 기사만 저장했는지 검증
        verify(articlesRepository, times(1)).save(articleCaptor.capture());

        Articles savedArticle = articleCaptor.getValue();
        assertEquals("https://naver.com/sample-2", savedArticle.getOriginalLink());

        // 중복 링크인 "https://naver.com/sample-1" 저장 안 됐는지 검증
        verify(articlesRepository, never())
            .save(argThat(article -> article != null && "https://naver.com/sample-1".equals(article.getOriginalLink())));
    }

}
