package org.project.monewping.domain.article.storage;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.project.monewping.domain.article.dto.data.ArticleDto;

@DisplayName("BackupStorage 테스트")
public class ArticleBackupStorageTest {

    private LocalArticleBackupStorage storage;
    private ObjectMapper objectMapper;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        storage = new LocalArticleBackupStorage(objectMapper, tempDir.toString());
    }

    @Test
    @DisplayName("뉴스 기사 저장 후 정상적으로 로드되는지 확인")
    void saveAndLoad_success() {
        // given
        LocalDate date = LocalDate.of(2025, 7, 22);
        ArticleDto article = new ArticleDto(
            null, "source", "sourceUrl", "title",
            null, "summary", 0L, 0L, false
        );
        List<ArticleDto> articles = List.of(article);

        // when
        assertDoesNotThrow(() -> storage.save(date, articles));
        File savedFile = tempDir.resolve("articles-" + date + ".json").toFile();
        List<ArticleDto> loadedArticles = storage.load(date);

        // then
        assertTrue(savedFile.exists(), "백업 파일이 생성되어야 합니다.");
        assertNotNull(loadedArticles);
        assertEquals(1, loadedArticles.size());
        assertEquals("source", loadedArticles.get(0).source());
        assertEquals("title", loadedArticles.get(0).title());
    }

    @Test
    @DisplayName("존재하지 않는 날짜의 백업 파일 로드 시 빈 리스트 반환")
    void load_NotExistDate_returnsEmptyList() {
        // given
        LocalDate date = LocalDate.of(1999, 1, 1);

        // when
        List<ArticleDto> result = storage.load(date);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("null 날짜로 저장 시 IllegalArgumentException 발생")
    void save_null_Date_IllegalArgumentException() {
        // when
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> storage.save(null, List.of()));

        // then
        assertEquals("날짜는 null일 수 없습니다.", ex.getMessage());
    }

    @Test
    @DisplayName("미래 날짜로 저장 시 IllegalArgumentException 발생")
    void save_FutureDate_IllegalArgumentException() {
        // given
        LocalDate futureDate = LocalDate.now().plusDays(1);

        // when
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> storage.save(futureDate, List.of()));

        // then
        assertEquals("미래 날짜의 백업은 조회할 수 없습니다.", ex.getMessage());
    }

    @Test
    @DisplayName("null 날짜로 로드 시 IllegalArgumentException 발생")
    void load_null_Date_IllegalArgumentException() {
        // when
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> storage.load(null));

        // then
        assertEquals("날짜는 null일 수 없습니다.", ex.getMessage());
    }

    @Test
    @DisplayName("미래 날짜로 로드 시 IllegalArgumentException 발생")
    void load_FutureDate_IllegalArgumentException_throwsException_withMessage() {
        // given
        LocalDate futureDate = LocalDate.now().plusDays(10);

        // when
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> storage.load(futureDate));

        // then
        assertEquals("미래 날짜의 백업은 조회할 수 없습니다.", ex.getMessage());
    }
}