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
    public void registerView(ArticleViewDto dto) {

        log.info("기사 조회 등록 요청 : userId = {}, articleId = {}", dto.userId(), dto.articleId());

        // 중복 여부 검사
        validateNoDuplicateViewHistory(dto.userId(), dto.articleId());

        NewsViewHistory newsViewHistory = newsViewHistoryMapper.toEntity(dto);
        newsViewHistoryRepository.save(newsViewHistory);

        log.info("기사 조회 기록 저장 완료 : userId = {}, articleId = {}, viewedAt = {}",
            dto.userId(), dto.articleId(), dto.viewedAt());
    }

    // 동일 기사 중복 조회 검사 메서드
    private void validateNoDuplicateViewHistory(UUID userId, UUID articleId) {
        if (newsViewHistoryRepository.findByUserIdAndArticleId(userId, articleId).isPresent()) {
            log.warn("중복 기사 조회 시도 감지 : userId = {}, articleId = {}", userId, articleId);
            throw new DuplicateViewHistoryException();
        }
    }
}
