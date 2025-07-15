package org.project.monewping.domain.article.controller;

import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.project.monewping.domain.article.dto.data.ArticleViewDto;
import org.project.monewping.domain.article.service.ArticleViewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ArticleController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
public class ArticlesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ArticleViewsService articleViewsService;

    @Test
    @DisplayName("기사 뷰 등록 성공 - 200 OK, 반환 데이터 검증")
    void RegisterArticleView_Success() throws Exception {
        // given
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

        // when
        doReturn(mockResponse).when(articleViewsService).registerView(userId, articleId);

        // then
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
        UUID articleId = UUID.randomUUID();

        mockMvc.perform(post("/api/articles/{articleId}/article-views", articleId)
                // 헤더 없음
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("기사 뷰 등록 실패 - 잘못된 UUID 경로 변수 시 400 Bad Request")
    void RegisterArticleView_InvalidUUIDPath_BadRequest() throws Exception {
        String invalidArticleId = "not-a-uuid";
        UUID userId = UUID.randomUUID();

        mockMvc.perform(post("/api/articles/{articleId}/article-views", invalidArticleId)
                .header("Monew-Request-User-ID", userId.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }
}
