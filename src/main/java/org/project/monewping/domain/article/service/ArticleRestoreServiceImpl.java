package org.project.monewping.domain.article.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.monewping.domain.article.dto.data.ArticleDto;
import org.project.monewping.domain.article.dto.response.ArticleRestoreResultDto;
import org.project.monewping.domain.article.entity.Articles;
import org.project.monewping.domain.article.mapper.ArticlesMapper;
import org.project.monewping.domain.article.repository.ArticlesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 백업된 뉴스 기사 JSON 파일로부터 복구 작업을 수행하는 서비스입니다.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ArticleRestoreServiceImpl implements ArticleRestoreService {

    private final ArticlesRepository articlesRepository;
    private final ArticlesMapper articlesMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 주어진 날짜에 해당하는 백업 파일을 읽고, 아직 저장되지 않은 뉴스 기사를 복원합니다.
     *
     * @param date 복원 대상 날짜 (예: 2025-07-17)
     * @return 복원된 기사 ID 목록 및 개수를 담은 결과 DTO
     */
    @Override
    public ArticleRestoreResultDto restoreArticlesByDate(LocalDate date) {

        String filename = "backup/news-" + date + ".json";
        List<ArticleDto> backupDtos;

        // 1. JSON 파일 로드
        try {
            backupDtos = objectMapper.readValue(
                new File(filename),
                new TypeReference<List<ArticleDto>>() {}
            );
        } catch (IOException e) {
            log.error("❌ 복구 실패 - 파일 읽기 오류", e);
            throw new RuntimeException("복구 JSON 파일 읽기 실패", e);
        }

        // 2. 기존 저장된 originalLink 목록 조회
        List<String> existingLinks = articlesRepository.findAllByOriginalLinkIn(
                backupDtos.stream().map(ArticleDto::sourceUrl).toList()
            ).stream()
            .map(Articles::getOriginalLink)
            .toList();

        // 3. 중복되지 않은 기사만 필터링
        List<ArticleDto> toRestore = backupDtos.stream()
            .filter(dto -> !existingLinks.contains(dto.sourceUrl()))
            .toList();

        // 4. 엔티티로 변환 및 저장
        List<Articles> restoredEntities = toRestore.stream()
            .map(articlesMapper::toEntity)
            .collect(Collectors.toList());
        articlesRepository.saveAll(restoredEntities);

        // 5. 복원 결과 리턴
        List<String> restoredIds = restoredEntities.stream()
            .map(article -> article.getId().toString())
            .toList();

        log.info("✅ 복구 완료: 날짜 = {}, 복구 수 = {}", date, restoredIds.size());
        return new ArticleRestoreResultDto(date, restoredIds, restoredIds.size());
    }

}
