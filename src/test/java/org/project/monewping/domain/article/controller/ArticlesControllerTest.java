package org.project.monewping.domain.article.controller;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.project.monewping.domain.article.dto.data.ArticleViewDto;
import org.project.monewping.domain.article.exception.DuplicateArticleViewsException;
import org.project.monewping.domain.article.service.ArticleViewsService;
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

}
