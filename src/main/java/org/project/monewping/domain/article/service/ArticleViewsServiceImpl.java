package org.project.monewping.domain.article.service;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.monewping.domain.article.dto.data.ArticleViewDto;
import org.project.monewping.domain.article.entity.ArticleViews;
import org.project.monewping.domain.article.entity.Articles;
import org.project.monewping.domain.article.exception.ArticleNotFoundException;
import org.project.monewping.domain.article.exception.DuplicateArticleViewsException;
import org.project.monewping.domain.article.repository.ArticleViewsRepository;
import org.project.monewping.domain.article.repository.ArticlesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 뉴스 기사 조회 기록 등록 서비스 구현체.
 *
 * <p>
 * 사용자가 특정 기사를 조회하면 다음을 수행한다:
 * <ul>
 *     <li>동일한 사용자-기사 조합의 중복 조회 여부 확인</li>
 *     <li>조회 기록을 저장</li>
 *     <li>기사 정보와 함께 DTO 반환</li>
 * </ul>
 * </p>
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ArticleViewsServiceImpl implements ArticleViewsService {

    private final ArticleViewsRepository articleViewsRepository;
    private final ArticlesRepository articlesRepository;

    /**
     * Registers a view record for a news article by a specific user.
     *
     * Creates and saves a new article view entry if the user has not already viewed the article. Returns a DTO containing the view record and article details.
     *
     * @param viewedBy the unique identifier of the user viewing the article
     * @param articleId the unique identifier of the article being viewed
     * @return a DTO representing the registered article view, including article and view details
     * @throws DuplicateArticleViewsException if a view record for this user and article already exists
     * @throws ArticleNotFoundException if the specified article does not exist
     */
    @Override
    public ArticleViewDto registerView(UUID viewedBy, UUID articleId) {

        log.info("기사 조회 등록 요청 : viewedBy = {}, articleId = {}", viewedBy, articleId);

        // 중복 여부 검사
        validateNoDuplicateViewHistory(viewedBy, articleId);

        // 1. 기사 조회
        Articles article = articlesRepository.findById(articleId)
            .orElseThrow(() -> {
                log.warn("존재하지 않는 기사 조회 시도 : articleId = {}", articleId);
                return new ArticleNotFoundException(articleId);
            });

        // 2. 엔티티 생성 및 저장
        log.info("기사 조회 정보 생성 : viewedBy = {}, articleId = {}, createdAt = {}", viewedBy, articleId, LocalDateTime.now());
        ArticleViews articleViews = ArticleViews.builder()
            .id(UUID.randomUUID())
            .viewedBy(viewedBy)
            .article(article)
            .createdAt(LocalDateTime.now())
            .build();

        log.info("기사 조회 정보 저장 : viewedBy = {}, articleId = {}", viewedBy, articleId);
        ArticleViews saved = articleViewsRepository.save(articleViews);

        // 3. DTO 생성 및 반환
        log.info("뉴스 기사 정보 응답 : viewedBy = {}, articleId = {}, source = {}, title = {}",
            viewedBy, articleId, article.getSource(), article.getTitle());
        return new ArticleViewDto(
            saved.getId(),
            viewedBy,
            saved.getCreatedAt(),
            article.getId(),
            article.getSource(),
            article.getOriginalLink(),
            article.getTitle(),
            article.getPublishedAt(),
            article.getSummary(),
            article.getCommentCount(),
            article.getViewCount()
        );
    }

    /**
     * Checks if a view record already exists for the given user and article combination.
     *
     * @param viewedBy the unique identifier of the user
     * @param articleId the unique identifier of the article
     * @throws DuplicateArticleViewsException if a view record for this user and article already exists
     */
    private void validateNoDuplicateViewHistory(UUID viewedBy, UUID articleId) {
        boolean exists = articleViewsRepository.findByViewedByAndArticleId(viewedBy, articleId).isPresent();
        if (exists) {
            log.warn("중복 기사 조회 시도 감지 : viewedBy = {}, articleId = {}", viewedBy, articleId);
            throw new DuplicateArticleViewsException();
        }
    }
}
