package org.project.monewping.domain.article.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.project.monewping.domain.article.dto.data.ArticleDto;
import org.project.monewping.domain.article.dto.data.ArticleViewDto;
import org.project.monewping.domain.article.dto.response.ArticleRestoreResultDto;
import org.project.monewping.domain.article.exception.ArticleNotFoundException;
import org.project.monewping.domain.article.exception.DuplicateArticleViewsException;
import org.project.monewping.domain.article.service.ArticleRestoreService;
import org.project.monewping.domain.article.service.ArticleViewsService;
import org.project.monewping.domain.article.service.ArticlesService;
import org.project.monewping.global.dto.CursorPageResponse;
import org.project.monewping.global.exception.GlobalExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@DisplayName("ArticlesController 테스트")
@WebMvcTest(controllers = ArticleController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@Import(GlobalExceptionHandler.class)
public class ArticlesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ArticleViewsService articleViewsService;

    @MockitoBean
    private ArticlesService articlesService;

    @MockitoBean
    private ArticleRestoreService articleRestoreService;

    @Test
    @DisplayName("기사 뷰 등록 성공 - 200 OK, 반환 데이터 검증")
    void RegisterArticleView_Success() throws Exception {
        // Given: 사용자 ID, 기사 ID, 뷰 ID, 응답 DTO 준비
        UUID articleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID viewId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        ArticleViewDto mockResponse = new ArticleViewDto(
            viewId,
            userId,
            now,
            articleId,
            "Naver",
            "https://news.naver.com/sample",
            "테스트 기사 제목",
            now.minusDays(1),
            "뉴스 기사 요약",
            5L,
            100L
        );

        // When: 서비스가 정상 응답을 반환하도록 설정
        doReturn(mockResponse).when(articleViewsService).registerView(userId, articleId);

        // Then: MockMvc로 POST 요청 시 200 OK와 JSON 응답 값 검증
        mockMvc.perform(post("/api/articles/{articleId}/article-views", articleId)
                .header("Monew-Request-User-ID", userId.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(viewId.toString()))
            .andExpect(jsonPath("$.viewedBy").value(userId.toString()))
            .andExpect(jsonPath("$.articleId").value(articleId.toString()))
            .andExpect(jsonPath("$.articlePublishedDate").exists());
    }

    @Test
    @DisplayName("기사 뷰 등록 실패 - 요청 헤더 누락 시 400 Bad Request")
    void RegisterArticleView_MissingHeader_BadRequest() throws Exception {
        // Given: 기사 ID만 존재 (헤더 없음)
        UUID articleId = UUID.randomUUID();

        // When: 사용자 ID 헤더 없이 요청
        // Then: 400 Bad Request 반환
        mockMvc.perform(post("/api/articles/{articleId}/article-views", articleId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("기사 뷰 등록 실패 - 잘못된 UUID 경로 변수 시 400 Bad Request")
    void RegisterArticleView_InvalidUUIDPath_BadRequest() throws Exception {
        // Given: articleId가 유효하지 않은 문자열일 때
        String invalidArticleId = "not-a-uuid";
        UUID userId = UUID.randomUUID();

        // When: 잘못된 UUID로 요청
        // Then: 400 Bad Request 반환
        mockMvc.perform(post("/api/articles/{articleId}/article-views", invalidArticleId)
                .header("Monew-Request-User-ID", userId.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("중복 조회 시 409 Conflict 응답")
    void RegisterArticleView_Conflict() throws Exception {
        // Given: 이미 등록된 뷰에 대해 요청
        UUID articleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        // When: 서비스가 중복 예외를 던지도록 설정
        when(articleViewsService.registerView(userId, articleId))
            .thenThrow(new DuplicateArticleViewsException());

        // Then: 409 Conflict 응답 확인
        mockMvc.perform(post("/api/articles/{articleId}/article-views", articleId)
                .header("Monew-Request-User-ID", userId.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("뉴스 기사 목록 조회 성공 - 200 OK, 결과 JSON 검증")
    void getArticles_Success() throws Exception {
        UUID sampleArticleId = UUID.randomUUID();
        UUID sampleUserId = UUID.randomUUID();

        ArticleDto sampleDto = new ArticleDto(
            sampleArticleId,
            "NAVER",
            "https://news.naver.com/sample-article",
            "테스트 뉴스 제목",
            LocalDateTime.now(),
            "테스트 요약",
            5L,
            100L,
            true
        );

        CursorPageResponse<ArticleDto> responseDto = new CursorPageResponse<>(
            List.of(sampleDto),
            null,
            null,
            1,
            1,
            false
        );

        when(articlesService.findArticles(any())).thenReturn(responseDto);

        mockMvc.perform(get("/api/articles")
                .param("keyword", "스포츠")
                .param("interestId", UUID.randomUUID().toString())
                .param("sourceIn", "NAVER")
                .param("publishDateFrom", "2025-01-01T00:00:00")
                .param("publishDateTo", "2025-12-31T23:59:59")
                .param("orderBy", "publishDate")
                .param("direction", "DESC")
                .param("limit", "10")
                .header("Monew-Request-User-ID", sampleUserId.toString())
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].id").value(sampleArticleId.toString()))
            .andExpect(jsonPath("$.content[0].source").value("NAVER"))
            .andExpect(jsonPath("$.content[0].title").value("테스트 뉴스 제목"))
            .andExpect(jsonPath("$.size").value(1))
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    @DisplayName("뉴스 기사 목록 조회 실패 - 필수 헤더 누락 시 400 Bad Request")
    void getArticles_MissingUserIdHeader_BadRequest() throws Exception {
        mockMvc.perform(get("/api/articles")
                .param("orderBy", "publishDate")
                .param("direction", "DESC")
                .param("limit", "10")
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("뉴스 기사 목록 조회 실패 - 필수 정렬 파라미터 누락 시 400 Bad Request")
    void getArticles_MissingOrderParams_BadRequest() throws Exception {
        UUID sampleUserId = UUID.randomUUID();

        mockMvc.perform(get("/api/articles")
                .param("limit", "10")
                .header("Monew-Request-User-ID", sampleUserId.toString())
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("출처 목록 조회 성공 - 삭제된 기사 제외, 중복 없이 반환")
    void getAllSources_Success() throws Exception {
        List<String> mockSources = List.of("NAVER", "중앙일보", "연합뉴스");

        // articlesService.getAllSources()가 mockSources를 반환하도록 설정
        doReturn(mockSources).when(articlesService).getAllSources();

        mockMvc.perform(get("/api/articles/sources")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(mockSources.size()))
            .andExpect(jsonPath("$[0]").value("NAVER"))
            .andExpect(jsonPath("$[1]").value("중앙일보"))
            .andExpect(jsonPath("$[2]").value("연합뉴스"));
    }

    @Test
    @DisplayName("출처 목록 조회 실패 - 서버 내부 오류 발생 시 500 반환")
    void getAllSources_InternalServerError() throws Exception {
        // articlesService.getAllSources() 호출 시 예외 발생하도록 설정
        when(articlesService.getAllSources()).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/api/articles/sources")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("복구 API는 유효한 날짜 범위 요청 시 200 OK와 결과를 반환한다")
    void restoreArticles_success() throws Exception {
        // given
        LocalDate from = LocalDate.of(2025, 7, 16);
        LocalDate to = LocalDate.of(2025, 7, 18);

        ArticleRestoreResultDto result = new ArticleRestoreResultDto(
            from.atStartOfDay(),
            List.of("id-1", "id-2"),
            2
        );

        when(articleRestoreService.restoreArticlesByRange(from, to))
            .thenReturn(List.of(result));

        // when & then
        mockMvc.perform(get("/api/articles/restore")
                .param("from", from.toString())
                .param("to", to.toString()))
            .andExpect(status().isOk());

        // then
        verify(articleRestoreService).restoreArticlesByRange(from, to);
    }

    @Test
    @DisplayName("복구 API는 from이 to보다 이후일 경우 400 Bad Request를 반환한다")
    void restoreArticles_invalidDateRange_returnsBadRequest() throws Exception {
        // given
        String from = "2025-07-19";
        String to = "2025-07-18";

        // when & then
        mockMvc.perform(get("/api/articles/restore")
                .param("from", from)
                .param("to", to))
            .andExpect(status().isBadRequest());

        // then
        verifyNoInteractions(articleRestoreService);
    }

    @Test
    @DisplayName("복구 API는 파라미터 누락 시 400 Bad Request를 반환한다")
    void restoreArticles_missingParams_returnsBadRequest() throws Exception {
        // given
        String validDate = "2025-07-18";

        // when & then: 'to' 누락
        mockMvc.perform(get("/api/articles/restore")
                .param("from", validDate))
            .andExpect(status().isBadRequest());

        // when & then: 'from' 누락
        mockMvc.perform(get("/api/articles/restore")
                .param("to", validDate))
            .andExpect(status().isBadRequest());

        // then
        verifyNoInteractions(articleRestoreService);
    }

    @Test
    @DisplayName("논리 삭제 API 성공 시 204 반환")
    void softDeleteApi_Success() throws Exception {
        // Given
        UUID articleId = UUID.randomUUID();
        doNothing().when(articlesService).softDelete(articleId);

        // When
        mockMvc.perform(delete("/api/articles/{articleId}", articleId))
            .andExpect(status().isNoContent());

        // Then
        verify(articlesService).softDelete(articleId);
    }

    @Test
    @DisplayName("논리 삭제 API - 없는 기사 요청 시 404 반환")
    void softDeleteApi_NotFound() throws Exception {
        // Given
        UUID articleId = UUID.randomUUID();
        doThrow(new ArticleNotFoundException(articleId)).when(articlesService).softDelete(articleId);

        // When
        mockMvc.perform(delete("/api/articles/{articleId}", articleId))
            .andExpect(status().isNotFound());

        // Then
        verify(articlesService).softDelete(articleId);
    }

    @Test
    @DisplayName("물리 삭제 API 성공 시 204 반환")
    void hardDeleteApi_Success() throws Exception {
        // Given
        UUID articleId = UUID.randomUUID();
        doNothing().when(articlesService).hardDelete(articleId);

        // When
        mockMvc.perform(delete("/api/articles/{articleId}/hard", articleId))
            .andExpect(status().isNoContent());

        // Then
        verify(articlesService).hardDelete(articleId);
    }

    @Test
    @DisplayName("물리 삭제 API - 없는 기사 요청 시 404 반환")
    void hardDeleteApi_NotFound() throws Exception {
        // Given
        UUID articleId = UUID.randomUUID();
        doThrow(new ArticleNotFoundException(articleId)).when(articlesService).hardDelete(articleId);

        // When
        mockMvc.perform(delete("/api/articles/{articleId}/hard", articleId))
            .andExpect(status().isNotFound());

        // Then
        verify(articlesService).hardDelete(articleId);
    }

}
