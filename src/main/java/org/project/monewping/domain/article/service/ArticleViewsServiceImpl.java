package org.project.monewping.domain.article.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.monewping.domain.article.dto.data.ArticleViewDto;
import org.project.monewping.domain.article.entity.ArticleViews;
import org.project.monewping.domain.article.exception.DuplicateArticleViewsException;
import org.project.monewping.domain.article.mapper.ArticleViewsMapper;
import org.project.monewping.domain.article.repository.ArticleViewsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 뉴스 기사 조회 기록 등록 서비스 구현체.
 * <p>
 * 사용자가 특정 기사를 조회했을 때 중복 조회 여부를 검사하고,
 * 조회 기록을 저장한다.
 * </p>
 */

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ArticleViewsServiceImpl implements ArticleViewsService {

    private final ArticleViewsRepository articleViewsRepository;
    private final ArticleViewsMapper articleViewsMapper;

    /**
     * 뉴스 기사 조회 기록을 등록한다.
     * <p>
     * 동일한 사용자와 기사 조합의 조회 기록이 이미 존재하면
     * {@link DuplicateArticleViewsException} 예외를 던진다.
     * </p>
     *
     * @param dto 조회 기록 등록 요청 데이터 (사용자 ID, 기사 ID, 조회 시간 포함)
     * @return 저장된 조회 기록의 DTO
     * @throws DuplicateArticleViewsException 중복 조회 기록 존재 시 발생
     */
    @Override
    public ArticleViewDto registerView(ArticleViewDto dto) {

        log.info("기사 조회 등록 요청 : viewedBy = {}, articleId = {}", dto.viewedBy(), dto.articleId());

        // 중복 여부 검사
        validateNoDuplicateViewHistory(dto.viewedBy(), dto.articleId());

        ArticleViews articleViews = articleViewsMapper.toEntity(dto);
        ArticleViews saved = articleViewsRepository.save(articleViews);

        log.info("기사 조회 기록 저장 완료 : viewedBy = {}, articleId = {}, articlePublishedDate = {}",
            dto.viewedBy(), dto.articleId(), dto.articlePublishedDate());

        return articleViewsMapper.toDto(saved);
    }

    /**
     * 동일 사용자와 기사 조합의 조회 기록 중복 여부를 검사한다.
     *
     * @param viewedBy 사용자 ID
     * @param articleId 뉴스 기사 ID
     * @throws DuplicateArticleViewsException 중복 조회 기록이 존재할 경우 발생
     */
    private void validateNoDuplicateViewHistory(UUID viewedBy, UUID articleId) {
        boolean exists = articleViewsRepository.findByViewedByAndArticleId(viewedBy, articleId).isPresent();
        if (exists) {
            log.warn("중복 기사 조회 시도 감지 : viewedBy = {}, articleId = {}", viewedBy, articleId);
            throw new DuplicateArticleViewsException();
        }
    }
}
