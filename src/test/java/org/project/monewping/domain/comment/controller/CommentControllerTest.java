package org.project.monewping.domain.comment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.monewping.domain.comment.dto.CommentResponseDto;
import org.project.monewping.domain.comment.exception.CommentDeleteException;
import org.project.monewping.domain.comment.service.CommentService;
import org.project.monewping.global.dto.CursorPageResponse;
import org.project.monewping.global.exception.GlobalExceptionHandler;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentController 테스트")
class CommentControllerTest {

    @Mock
    private CommentService commentService;

    @InjectMocks
    private CommentController commentController;

    private MockMvc mockMvc;

    private UUID testArticleId;
    private List<CommentResponseDto> testComments;
    private CursorPageResponse<CommentResponseDto> testResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(commentController)
            .setControllerAdvice(new GlobalExceptionHandler()) // ✅ 예외 핸들러 추가!
            .build();

        testArticleId = UUID.randomUUID();

        testComments = Arrays.asList(
            new CommentResponseDto(
                UUID.randomUUID(),
                "첫 번째 댓글입니다.",
                "사용자1",
                5,
                Instant.now().minus(Duration.ofHours(1))
            ),
            new CommentResponseDto(
                UUID.randomUUID(),
                "두 번째 댓글입니다.",
                "사용자2",
                3,
                Instant.now().minus(Duration.ofHours(1))
            )
        );

        testResponse = new CursorPageResponse<>(
            testComments,
            123456L,
            "next_cursor_value",
            2,
            2L,
            true
        );
    }

    @Test
    @DisplayName("댓글 조회 성공 - 기본 파라미터")
    void getComments_Success_WithDefaultParameters() throws Exception {
        when(commentService.getComments(
            eq(testArticleId),
            eq("createdAt"),
            eq("DESC"),
            eq(null),
            eq(null),
            eq(50)
        )).thenReturn(testResponse);

        mockMvc.perform(get("/api/comments")
                .param("articleId", testArticleId.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[0].content").value("첫 번째 댓글입니다."))
            .andExpect(jsonPath("$.content[0].userNickname").value("사용자1"))
            .andExpect(jsonPath("$.content[0].likeCount").value(5))
            .andExpect(jsonPath("$.content[1].content").value("두 번째 댓글입니다."))
            .andExpect(jsonPath("$.content[1].userNickname").value("사용자2"))
            .andExpect(jsonPath("$.content[1].likeCount").value(3))
            .andExpect(jsonPath("$.nextCursor").value("next_cursor_value"))
            .andExpect(jsonPath("$.nextIdAfter").value(123456L))
            .andExpect(jsonPath("$.hasNext").value(true))
            .andExpect(jsonPath("$.size").value(2))
            .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @DisplayName("댓글 조회 성공 - 모든 파라미터 포함")
    void getComments_Success_WithAllParameters() throws Exception {
        String cursor = "test_cursor";
        String after = "2024-01-01T10:00:00";
        Integer limit = 20;

        when(commentService.getComments(
            eq(testArticleId),
            eq("likeCount"),
            eq("ASC"),
            eq(cursor),
            eq(after),
            eq(limit)
        )).thenReturn(testResponse);

        mockMvc.perform(get("/api/comments")
                .param("articleId", testArticleId.toString())
                .param("orderBy", "likeCount")
                .param("direction", "ASC")
                .param("cursor", cursor)
                .param("after", after)
                .param("limit", limit.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.nextCursor").value("next_cursor_value"))
            .andExpect(jsonPath("$.hasNext").value(true));
    }

    @Test
    @DisplayName("댓글 조회 성공 - 빈 결과")
    void getComments_Success_EmptyResult() throws Exception {
        CursorPageResponse<CommentResponseDto> emptyResponse = new CursorPageResponse<>(
            Arrays.asList(),
            null,
            null,
            0,
            0L,
            false
        );

        when(commentService.getComments(
            any(UUID.class),
            any(String.class),
            any(String.class),
            any(),
            any(),
            any(Integer.class)
        )).thenReturn(emptyResponse);

        mockMvc.perform(get("/api/comments")
                .param("articleId", testArticleId.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(0))
            .andExpect(jsonPath("$.nextCursor").doesNotExist())
            .andExpect(jsonPath("$.nextIdAfter").doesNotExist())
            .andExpect(jsonPath("$.hasNext").value(false))
            .andExpect(jsonPath("$.size").value(0))
            .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("댓글 조회 실패 - articleId 파라미터 누락")
    void getComments_Fail_MissingArticleId() throws Exception {
        mockMvc.perform(get("/api/comments")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("댓글 조회 실패 - 잘못된 UUID 형식")
    void getComments_Fail_InvalidUuidFormat() throws Exception {
        mockMvc.perform(get("/api/comments")
                .param("articleId", "invalid-uuid")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("댓글 조회 성공 - 커서 페이지네이션")
    void getComments_Success_WithCursor() throws Exception {
        String cursor = "cursor_value";

        when(commentService.getComments(
            eq(testArticleId),
            eq("createdAt"),
            eq("DESC"),
            eq(cursor),
            eq(null),
            eq(50)
        )).thenReturn(testResponse);

        mockMvc.perform(get("/api/comments")
                .param("articleId", testArticleId.toString())
                .param("cursor", cursor)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.nextCursor").value("next_cursor_value"))
            .andExpect(jsonPath("$.hasNext").value(true));
    }

    @Test
    @DisplayName("댓글 등록 성공")
    void registerComment_Success() throws Exception {
        String requestBody = String.format("""
        {
            "articleId": "%s",
            "userId": "%s",
            "content": "테스트 댓글입니다."
        }
        """, UUID.randomUUID(), UUID.randomUUID());

        mockMvc.perform(post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("댓글 논리 삭제 성공")
    void deleteComment_Success() throws Exception {
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        doNothing().when(commentService).deleteComment(eq(commentId), eq(userId));

        mockMvc.perform(delete("/api/comments/{commentId}", commentId)
                .param("userId", userId.toString()))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("댓글 논리 삭제 실패 - 본인 아님")
    void deleteComment_Fail_NotOwner() throws Exception {
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        doThrow(new CommentDeleteException("본인의 댓글만 삭제할 수 있습니다."))
            .when(commentService).deleteComment(eq(commentId), eq(userId));

        mockMvc.perform(delete("/api/comments/{commentId}", commentId)
                .param("userId", userId.toString()))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("댓글 물리 삭제 성공")
    void deleteCommentPhysically_Success() throws Exception {
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        doNothing().when(commentService).deleteCommentPhysically(eq(commentId), eq(userId));

        mockMvc.perform(delete("/api/comments/{commentId}/hard", commentId)
                .param("userId", userId.toString()))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("댓글 물리 삭제 실패 - 본인 아님")
    void deleteCommentPhysically_Fail_NotOwner() throws Exception {
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        doThrow(new CommentDeleteException("본인의 댓글만 삭제할 수 있습니다."))
            .when(commentService).deleteCommentPhysically(eq(commentId), eq(userId));

        mockMvc.perform(delete("/api/comments/{commentId}/hard", commentId)
                .param("userId", userId.toString()))
            .andExpect(status().isForbidden());
    }
}