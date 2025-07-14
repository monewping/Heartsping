package org.project.monewping.domain.article.service;

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
        // 동일 기사 중복 조회 시 예외 발생
        if (newsViewHistoryRepository.findByUserIdAndArticleId(dto.userId(), dto.articleId()).isPresent()) {
            throw new DuplicateViewHistoryException();
        }

        NewsViewHistory newsViewHistory = newsViewHistoryMapper.toEntity(dto);
        newsViewHistoryRepository.save(newsViewHistory);
    }
}
