package org.project.monewping.domain.comment.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.monewping.domain.comment.exception.CommentLikeAlreadyExistsException;
import org.project.monewping.domain.comment.exception.CommentLikeNotFoundException;
import org.project.monewping.domain.comment.service.CommentLikeService;
import org.project.monewping.global.exception.GlobalExceptionHandler;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@org.junit.jupiter.api.extension.ExtendWith(MockitoExtension.class)
@DisplayName("CommentLikeController 테스트")
class CommentLikeControllerTest {

    @Mock
    private CommentLikeService commentLikeService;

    @InjectMocks
    private CommentLikeController commentLikeController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(commentLikeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("댓글 좋아요 등록 성공")
    void likeComment_Success() throws Exception {
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        doNothing().when(commentLikeService).likeComment(userId, commentId);

        mockMvc.perform(post("/api/comments/{commentId}/comment-likes", commentId)
                        .header("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("댓글 좋아요 중복 등록 실패")
    void likeComment_AlreadyExists() throws Exception {
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        doThrow(new CommentLikeAlreadyExistsException())
                .when(commentLikeService).likeComment(userId, commentId);

        mockMvc.perform(post("/api/comments/{commentId}/comment-likes", commentId)
                        .header("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("댓글 좋아요 취소 성공")
    void unlikeComment_Success() throws Exception {
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        doNothing().when(commentLikeService).unlikeComment(userId, commentId);

        mockMvc.perform(delete("/api/comments/{commentId}/comment-likes", commentId)
                        .header("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("댓글 좋아요 취소 실패 - 존재하지 않음")
    void unlikeComment_NotFound() throws Exception {
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        doThrow(new CommentLikeNotFoundException())
                .when(commentLikeService).unlikeComment(userId, commentId);

        mockMvc.perform(delete("/api/comments/{commentId}/comment-likes", commentId)
                        .header("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}