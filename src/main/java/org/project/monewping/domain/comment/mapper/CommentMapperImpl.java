package org.project.monewping.domain.comment.mapper;

import org.project.monewping.domain.comment.domain.Comment;
import org.project.monewping.domain.comment.dto.CommentResponseDto;
import org.springframework.stereotype.Component;

/**
 * 댓글 Mapper 구현체
 */
@Component
public class CommentMapperImpl implements CommentMapper {

    @Override
    public CommentResponseDto toResponseDto(Comment comment) {
        if (comment == null) {
            return null;
        }
        return new CommentResponseDto(
            comment.getId(),
            comment.getContent(),
            comment.getUserNickname(),
            comment.getLikeCount(),
            comment.getCreatedAt().toString()
        );
    }
}