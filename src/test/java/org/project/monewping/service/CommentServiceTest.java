package org.project.monewping.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.monewping.domain.comment.domain.Comment;
import org.project.monewping.domain.comment.dto.CommentResponseDto;
import org.project.monewping.domain.comment.mapper.CommentMapper;
import org.project.monewping.domain.comment.repository.CommentRepository;
import org.project.monewping.domain.comment.service.CommentServiceImpl;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @InjectMocks
    private CommentServiceImpl commentService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentMapper commentMapper;

    @DisplayName("댓글 목록 조회 - 성공")
    @Test
    void 댓글_목록_조회_성공() {
        UUID articleId = UUID.randomUUID();
        Comment comment1 = Comment.builder()
            .id(1L)
            .content("테스트 댓글1")
            .nickname("닉네임1")
            .likeCount(3)
            .createdAt(LocalDateTime.now())
            .articleId(articleId)
            .build();
        Comment comment2 = Comment.builder()
            .id(2L)
            .content("테스트 댓글2")
            .nickname("닉네임2")
            .likeCount(5)
            .createdAt(LocalDateTime.now())
            .articleId(articleId)
            .build();

        given(commentRepository.findComments(articleId, "createdAt", "DESC", null, null, 50))
            .willReturn(List.of(comment1, comment2));

        given(commentMapper.toResponseDto(comment1)).willReturn(
            new CommentResponseDto(comment1.getId(), comment1.getContent(), comment1.getNickname(), comment1.getLikeCount(), comment1.getCreatedAt())
        );
        given(commentMapper.toResponseDto(comment2)).willReturn(
            new CommentResponseDto(comment2.getId(), comment2.getContent(), comment2.getNickname(), comment2.getLikeCount(), comment2.getCreatedAt())
        );

        var response = commentService.getComments(articleId, "createdAt", "DESC", null, null, 50);

        assertThat(response.content()).hasSize(2);
        assertThat(response.content().get(0).getContent()).isEqualTo("테스트 댓글1");
        assertThat(response.content().get(1).getContent()).isEqualTo("테스트 댓글2");
    }
}