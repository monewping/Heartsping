package org.project.monewping.domain.comment.mapper;

import org.project.monewping.domain.comment.domain.Comment;
import org.project.monewping.domain.comment.dto.CommentRegisterRequestDto;
import org.project.monewping.domain.comment.dto.CommentResponseDto;

/**
 * 댓글 Mapper 인터페이스
 */
public interface CommentMapper {
    Comment toEntity(CommentRegisterRequestDto requestDto, String userNickname);
    CommentResponseDto toResponseDto(Comment comment);
    CommentResponseDto toResponseDto(Comment comment, boolean likedByMe);
}