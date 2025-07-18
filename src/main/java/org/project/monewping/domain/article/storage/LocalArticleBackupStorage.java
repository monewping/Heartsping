package org.project.monewping.domain.article.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.monewping.domain.article.dto.data.ArticleDto;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j
@Component
public class LocalArticleBackupStorage implements ArticleBackupStorage {

    private final ObjectMapper objectMapper;

    private static final String BACKUP_DIR = "backup";

    @Override
    public List<ArticleDto> load(LocalDate date) {
        File file = new File(BACKUP_DIR, "articles-" + date.toString() + ".json");
        if (!file.exists()) {
            log.info("백업 파일이 존재하지 않습니다: {}", file.getAbsolutePath());
            return List.of();
        }
        try {
            return objectMapper.readValue(file, new TypeReference<List<ArticleDto>>() {});
        } catch (IOException e) {
            log.error("백업 파일 로드 실패: {}", file.getAbsolutePath(), e);
            throw new RuntimeException("복구 JSON 파일 읽기 실패", e);
        }
    }

    @Override
    public void save(LocalDate date, List<ArticleDto> articles) {
        File dir = new File(BACKUP_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, "articles-" + date.toString() + ".json");
        try {
            objectMapper.writeValue(file, articles);
            log.info("뉴스 기사 백업 완료: {}", file.getAbsolutePath());
        } catch (IOException e) {
            log.error("백업 파일 저장 실패: {}", file.getAbsolutePath(), e);
            throw new RuntimeException("백업 JSON 파일 저장 실패", e);
        }
    }
}
