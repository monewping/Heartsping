package org.project.monewping.domain.comment.mapper;

import java.time.Instant;
import org.project.monewping.domain.comment.domain.Comment;
import org.project.monewping.domain.comment.dto.CommentRegisterRequestDto;
import org.project.monewping.domain.comment.dto.CommentResponseDto;
import org.springframework.stereotype.Component;

/**
 * 댓글 Mapper 구현체
 */
@Component
public class CommentMapperImpl implements CommentMapper {

    @Override
    public Comment toEntity(CommentRegisterRequestDto requestDto, String userNickname) {
        if (requestDto == null) {
            return null;
        }
        Instant now = Instant.now();
        return Comment.builder()
            .articleId(requestDto.getArticleId())
            .userId(requestDto.getUserId())
            .userNickname(userNickname)
            .content(requestDto.getContent())
            .likeCount(0)
            .createdAt(now)
            .updatedAt(now)
            .isDeleted(false)
            .build();
    }

    @Override
    public CommentResponseDto toResponseDto(Comment comment) {
        return toResponseDto(comment, false); // 🔥 기본은 false로 위임
    }

    @Override
    public CommentResponseDto toResponseDto(Comment comment, boolean likedByMe) {
        if (comment == null) {
            return null;
        }
        return new CommentResponseDto(
            comment.getId(),
            comment.getArticleId(),
            comment.getUserId(),
            comment.getUserNickname(),
            comment.getContent(),
            comment.getLikeCount(),
            likedByMe,
            comment.getCreatedAt().toString()
        );
    }
}