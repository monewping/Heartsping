package org.project.monewping.domain.comment.mapper;

import org.project.monewping.domain.comment.domain.Comment;
import org.project.monewping.domain.comment.dto.CommentResponseDto;
import org.springframework.stereotype.Component;

/**
 * 댓글 매퍼
 * Comment 엔티티와 CommentResponseDto 간 변환을 담당한다.
 */
@Component
public class CommentMapper {
    /**
     * 댓글 엔티티를 응답 DTO로 변환
     *
     * @param comment 댓글 엔티티
     * @return 댓글 응답 DTO
     */
    public CommentResponseDto toResponseDto(Comment comment) {
        return CommentResponseDto.builder()
            .id(comment.getId())
            .content(comment.getContent())
            .likeCount(comment.getLikeCount())
            .createdAt(comment.getCreatedAt())
            .build();
  }
}