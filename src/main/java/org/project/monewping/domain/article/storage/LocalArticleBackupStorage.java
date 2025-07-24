package org.project.monewping.domain.article.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.project.monewping.domain.article.dto.data.ArticleDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LocalArticleBackupStorage implements ArticleBackupStorage {

    private final ObjectMapper objectMapper;
    private final String backupDir;

    /**
     * {@link LocalArticleBackupStorage} 생성자.
     *
     * @param objectMapper Jackson의 ObjectMapper로 JSON 직렬화/역직렬화에 사용됩니다.
     * @param backupDir 백업 파일을 저장할 디렉토리 경로. 설정되지 않으면 기본값 "backup"이 사용됩니다.
     */
    public LocalArticleBackupStorage(ObjectMapper objectMapper,
        @Value("${backup.dir:backup}") String backupDir) {
        this.objectMapper = objectMapper;
        this.backupDir = new File(backupDir).getAbsolutePath();
    }

    /**
     * 지정한 날짜에 해당하는 뉴스 기사 백업 파일을 로드합니다.
     *
     * @param date 백업 파일 날짜 (형식: YYYY-MM-DD)
     * @return 기사 목록. 해당 날짜의 백업 파일이 없으면 빈 리스트를 반환합니다.
     * @throws RuntimeException 파일 읽기 실패 시 예외를 던집니다.
     * @throws IllegalArgumentException 유효하지 않은 날짜 입력 시 예외를 던집니다.
     */
    @Override
    public List<ArticleDto> load(LocalDate date) {
        validateDate(date);
        String fileName = String.format("articles-%s.json", date);
        File file = new File(backupDir, fileName);

        if (!file.exists()) {
            log.info("백업 파일이 존재하지 않습니다: {}", file.getAbsolutePath());
            return List.of();
        }

        try {
            return objectMapper.readValue(file, new TypeReference<>() {});
        } catch (IOException e) {
            log.error("백업 파일 로드 실패: {}", file.getAbsolutePath(), e);
            throw new RuntimeException("백업 JSON 파일 읽기 실패: " + file.getName(), e);
        }
    }

    /**
     * 지정한 날짜에 뉴스 기사 목록을 JSON 형식으로 백업 파일로 저장합니다.
     * <p>
     * 저장 시 임시 파일에 먼저 기록하고, 기존 파일은 .bak 확장자로 백업한 뒤,
     * 임시 파일을 원자적으로 실제 백업 파일로 이동하여 안정성을 보장합니다.
     * </p>
     *
     * @param date 백업 날짜 (형식: YYYY-MM-DD)
     * @param articles 저장할 뉴스 기사 목록
     * @throws RuntimeException 파일 입출력 오류 및 디렉토리 생성 실패 시 발생
     * @throws IllegalArgumentException 날짜가 null이거나 미래인 경우 발생
     */
    @Override
    public void save(LocalDate date, List<ArticleDto> articles) {
        validateDate(date);

        File dir = new File(backupDir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException("백업 디렉토리 생성 실패: " + backupDir);
        }

        String fileName = String.format("articles-%s.json", date);
        File file = new File(dir, fileName);
        File tempFile = new File(dir, fileName + ".tmp");

        try {
            // 1) 임시 파일에 먼저 저장
            objectMapper.writeValue(tempFile, articles);

            // 2) 기존 파일이 있으면 백업(.bak)
            if (file.exists()) {
                File backupFile = new File(dir, fileName + ".bak");
                Files.move(file.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            // 3) 임시 파일을 원자적으로 실제 파일 위치로 이동
            Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.ATOMIC_MOVE);

            log.info("뉴스 기사 백업 완료: {}", file.getAbsolutePath());
        } catch (IOException e) {
            // 실패 시 임시 파일 삭제 시도
            if (tempFile.exists()) {
                try {
                    Files.delete(tempFile.toPath());
                } catch (IOException ex) {
                    log.warn("임시 백업 파일 삭제 실패: {}", tempFile.getAbsolutePath(), ex);
                }
            }
            log.error("백업 파일 저장 실패: {}", file.getAbsolutePath(), e);
            throw new RuntimeException("백업 JSON 파일 저장 실패: " + fileName, e);
        }
    }

    /**
     * 날짜 유효성을 검증합니다.
     *
     * @param date 검사할 날짜
     * @throws IllegalArgumentException 날짜가 null이거나 미래일 경우 예외 발생
     */
    private void validateDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("날짜는 null일 수 없습니다.");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("미래 날짜의 백업은 조회할 수 없습니다.");
        }
    }
}