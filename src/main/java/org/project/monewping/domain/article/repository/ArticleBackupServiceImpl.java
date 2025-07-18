package org.project.monewping.domain.article.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.monewping.domain.article.dto.data.ArticleDto;
import org.project.monewping.domain.article.entity.Articles;
import org.project.monewping.domain.article.mapper.ArticlesMapper;
import org.project.monewping.domain.article.service.ArticleBackupService;
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
    private final ArticlesMapper articlesMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 지정된 날짜에 해당하는 뉴스 기사 데이터를 백업합니다.
     *
     * <p>백업 대상은 해당 날짜에 발행되었으며, 삭제되지 않은 기사입니다.
     * 결과는 JSON 파일로 저장되며, 저장 위치는 {@code backup/news-YYYY-MM-DD.json}입니다.</p>
     *
     * @param date 백업할 날짜 (YYYY-MM-DD 기준)
     * @throws RuntimeException 백업 과정 중 파일 저장 오류 발생 시 예외 발생
     */
    @Override
    public void backupArticlesByDate(LocalDate date) {

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        List<Articles> articles = articlesRepository.findAllByPublishedAtBetweenAndDeletedFalse(start, end);
        List<ArticleDto> backupDtos = articles.stream()
            .map(articlesMapper::toDto)
            .collect(Collectors.toList());

        String backupDir = "backup";
        new File(backupDir).mkdirs(); // 디렉토리 없으면 생성

        String filename = backupDir + "/news-" + date + ".json";
        try (FileWriter writer = new FileWriter(filename)) {
            objectMapper.writeValue(writer, backupDtos);
            log.info("✅ 백업 완료: 날짜 = {}, 수량 = {}, 파일 = {}", date, backupDtos.size(), filename);
        } catch (IOException e) {
            log.error("❌ 백업 실패", e);
            throw new RuntimeException("백업 중 오류 발생", e);
        }
    }

}
