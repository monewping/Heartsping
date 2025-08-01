package org.project.monewping.domain.article.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.project.monewping.MonewpingApplication;
import org.project.monewping.domain.article.config.WebConfig;
import org.project.monewping.domain.article.converter.StringToLocalDateTimeConverter;
import org.project.monewping.domain.article.dto.data.ArticleDto;
import org.project.monewping.domain.article.entity.Articles;
import org.project.monewping.domain.article.repository.ArticleViewsRepository;
import org.project.monewping.domain.article.repository.ArticlesRepository;
import org.project.monewping.domain.article.storage.S3ArticleBackupStorage;
import org.project.monewping.domain.interest.entity.Interest;
import org.project.monewping.domain.interest.repository.InterestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = MonewpingApplication.class)
@EnableAutoConfiguration(
    exclude = {
        SecurityAutoConfiguration.class,
        ManagementWebSecurityAutoConfiguration.class
    }
)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@Import({TestS3MockConfig.class, WebConfig.class, StringToLocalDateTimeConverter.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Articles 통합 테스트")
public class ArticlesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ArticlesRepository articlesRepository;

    @Autowired
    private ArticleViewsRepository articleViewsRepository;

    @Autowired
    private InterestRepository interestRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private S3ArticleBackupStorage s3ArticleBackupStorage;

    private UUID testUserId;
    private Articles testArticle;

    @BeforeEach
    void setup() {
        testUserId = UUID.randomUUID();

        // Interest 저장 후 재조회
        Interest interest = Interest.builder()
            .name("테스트관심사")
            .build();
        interest = interestRepository.saveAndFlush(interest);

        // Articles 빌드 (id 수동 할당 제거 권장, 자동 생성 설정이 있다면)
        Articles article = Articles.builder()
            .interest(interest)
            .source("TestSource")
            .originalLink("https://test.com/article/1")
            .title("테스트 기사 제목")
            .summary("테스트 기사 요약")
            .publishedAt(LocalDateTime.now().minusDays(1))
            .commentCount(5)
            .viewCount(0)
            .deleted(false)
            .build();

        testArticle = articlesRepository.saveAndFlush(article);
    }


    @Test
    @DisplayName("뉴스 기사 조회 기록 등록 및 중복 조회 방지 테스트")
    void testRegisterArticleView() throws Exception {
        // 첫 조회 기록 등록 요청
        mockMvc.perform(post("/api/articles/{articleId}/article-views", testArticle.getId())
                .header("Monew-Request-User-ID", testUserId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.articleId").value(testArticle.getId().toString()))
            .andExpect(jsonPath("$.viewedBy").value(testUserId.toString()));

        // 중복 등록 시 409 Conflict 예상
        mockMvc.perform(post("/api/articles/{articleId}/article-views", testArticle.getId())
                .header("Monew-Request-User-ID", testUserId))
            .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("뉴스 기사 목록 조회 테스트")
    void testGetArticles() throws Exception {
        mockMvc.perform(get("/api/articles")
                .param("orderBy", "publishDate")
                .param("direction", "DESC")
                .param("limit", "10")
                .header("Monew-Request-User-ID", testUserId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].id").value(testArticle.getId().toString()));
    }

    @Test
    @DisplayName("뉴스 기사 출처 목록 조회 테스트")
    void testGetAllSources() throws Exception {
        mockMvc.perform(get("/api/articles/sources"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasItem("TestSource")));
    }

    @Test
    @DisplayName("뉴스 기사 논리 삭제 후, 조회 시 삭제된 기사는 조회되지 않아야 한다")
    void testSoftDeleteArticle() throws Exception {
        // 논리 삭제 요청
        mockMvc.perform(delete("/api/articles/{articleId}", testArticle.getId()))
            .andExpect(status().isNoContent());

        // 논리 삭제된 기사는 deleted = true 이므로, findByIdAndDeletedFalse로 조회하면 Optional.empty
        Optional<Articles> deletedArticleOpt = articlesRepository.findByIdAndDeletedFalse(testArticle.getId());

        // 삭제된 기사는 조회되지 않아야 함
        assertThat(deletedArticleOpt).isEmpty();

        // 원래 findById로 조회하면 존재는 해야 함 (데이터가 물리 삭제된 건 아니므로)
        Articles deletedArticle = articlesRepository.findById(testArticle.getId())
            .orElseThrow(() -> new AssertionError("삭제된 기사를 DB에서 찾을 수 없습니다."));

        assertThat(deletedArticle.isDeleted()).isTrue();
    }


    @Test
    @DisplayName("뉴스 기사 물리 삭제 테스트")
    void testHardDeleteArticle() throws Exception {
        Articles newArticle = Articles.builder()
            .interest(testArticle.getInterest())
            .source("TestSource2")
            .originalLink("https://test.com/art/2")
            .title("물리 삭제 테스트")
            .summary("요약")
            .publishedAt(LocalDateTime.now())
            .commentCount(0)
            .viewCount(0)
            .deleted(false)
            .build();

        articlesRepository.saveAndFlush(newArticle);

        mockMvc.perform(delete("/api/articles/{articleId}/hard", newArticle.getId()))
            .andExpect(status().isNoContent());

        articlesRepository.flush();  // 삭제 반영 강제

        // 삭제 확인용 조회만 하세요
        Assertions.assertFalse(articlesRepository.existsById(newArticle.getId()));
    }



    @Test
    @DisplayName("뉴스 기사 복구 API 테스트 (날짜 범위) - 단일 날짜 범위, 최소 데이터 사용")
    void testRestoreArticles() throws Exception {
        // 단일 날짜 (예: 2025-07-05)로 범위 제한
        LocalDate testDate = LocalDate.of(2025, 7, 5);

        List<ArticleDto> mockArticles = List.of(
            new ArticleDto(
                UUID.randomUUID(),
                "TestSource",
                "https://test.com/article/unique-url",
                "테스트 기사 제목",
                LocalDateTime.of(2025, 7, 5, 10, 30),
                "테스트 기사 요약",
                10L,
                100L,
                false
            )
        );

        Mockito.when(s3ArticleBackupStorage.load(Mockito.any(LocalDate.class)))
            .thenAnswer(invocation -> {
                LocalDate date = invocation.getArgument(0);
                if (date.equals(testDate)) {
                    return mockArticles;
                } else {
                    return List.of();
                }
            });

        mockMvc.perform(get("/api/articles/restore")
                .param("from", "2025-07-05")
                .param("to", "2025-07-05"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].restoredArticleCount").value(1));
    }

}
