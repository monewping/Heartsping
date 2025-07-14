package org.project.monewping.domain.article.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.monewping.domain.article.dto.data.ArticleViewDto;
import org.project.monewping.domain.article.entity.NewsViewHistory;
import org.project.monewping.domain.article.exception.DuplicateViewHistoryException;
import org.project.monewping.domain.article.mapper.NewsViewHistoryMapper;
import org.project.monewping.domain.article.repository.NewsViewHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ArticleViewServiceImpl implements ArticleViewService {

    private final NewsViewHistoryRepository newsViewHistoryRepository;
    private final NewsViewHistoryMapper newsViewHistoryMapper;

    @Override
    public ArticleViewDto registerView(ArticleViewDto dto) {

        log.info("기사 조회 등록 요청 : viewedBy = {}, articleId = {}", dto.viewedBy(), dto.articleId());

        // 중복 여부 검사
        validateNoDuplicateViewHistory(dto.viewedBy(), dto.articleId());

        NewsViewHistory newsViewHistory = newsViewHistoryMapper.toEntity(dto);
        NewsViewHistory saved = newsViewHistoryRepository.save(newsViewHistory);

        log.info("기사 조회 기록 저장 완료 : viewedBy = {}, articleId = {}, articlePublishedDate = {}",
            dto.viewedBy(), dto.articleId(), dto.articlePublishedDate());

        return newsViewHistoryMapper.toDto(saved);
    }

    // 동일 기사 중복 조회 검사 메서드
    private void validateNoDuplicateViewHistory(UUID viewedBy, UUID articleId) {
        boolean exists = newsViewHistoryRepository.findByViewedByAndArticleId(viewedBy, articleId).isPresent();
        if (exists) {
            log.warn("중복 기사 조회 시도 감지 : viewedBy = {}, articleId = {}", viewedBy, articleId);
            throw new DuplicateViewHistoryException();
        }
    }
}
