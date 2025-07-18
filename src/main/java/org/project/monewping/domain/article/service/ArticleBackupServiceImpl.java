package org.project.monewping.domain.article.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.monewping.domain.article.dto.data.ArticleDto;
import org.project.monewping.domain.article.entity.Articles;
import org.project.monewping.domain.article.mapper.ArticlesMapper;
import org.project.monewping.domain.article.repository.ArticlesRepository;
import org.project.monewping.domain.article.storage.ArticleBackupStorage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * 특정 날짜 기준으로 뉴스 기사를 백업하는 서비스 구현체입니다.
 * 향후 AWS S3로의 리팩토링을 고려하여 작성되었습니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ArticleBackupServiceImpl implements ArticleBackupService {

    private final ArticlesRepository articlesRepository;
    private final ArticleBackupStorage backupStorage;
    private final ArticlesMapper articlesMapper;

    /**
     * 지정된 날짜의 뉴스를 백업합니다.
     *
     * @param date 백업 대상 날짜 (yyyy-MM-dd)
     */
    @Override
    public void backupArticlesByDate(LocalDate date) {
        log.info("뉴스 기사 백업 시작 = 날짜 : {}", date);

        // 해당 날짜에 삭제되지 않은 기사 목록 조회
        List<Articles> articles = articlesRepository.findByPublishedAtBetweenAndDeletedFalse(
            date.atStartOfDay(), date.plusDays(1).atStartOfDay()
        );

        // 엔티티 → DTO 변환
        List<ArticleDto> dtos = articles.stream()
            .map(articlesMapper::toDto)
            .collect(Collectors.toList());

        // DTO 리스트를 백업 저장소에 저장
        backupStorage.save(date, dtos);

        log.info("뉴스 기사 백업 완료 = 날짜 : {}, 건수 : {}", date, dtos.size());
    }

}
