package org.project.monewping.domain.comment.mapper;

import org.mapstruct.Mapper;
import org.project.monewping.domain.comment.domain.Comment;
import org.project.monewping.domain.comment.dto.CommentResponseDto;

/**
 * 댓글 매퍼
 * Comment 엔티티와 CommentResponseDto 간 변환을 담당합니다.
 */
@Mapper(componentModel = "spring")
public interface CommentMapper {
    CommentResponseDto toResponseDto(Comment comment);
}