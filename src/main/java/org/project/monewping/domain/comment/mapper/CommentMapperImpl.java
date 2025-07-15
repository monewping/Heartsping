package org.project.monewping.domain.comment.mapper;

import java.time.Instant;
import java.util.UUID;
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
    public Comment toEntity(CommentRegisterRequestDto requestDto) {
        if (requestDto == null) {
            return null;
        }
        return Comment.builder()
            .id(UUID.randomUUID())
            .articleId(requestDto.getArticleId())
            .userId(requestDto.getUserId())
            .userNickname("익명")  // 임시
            .content(requestDto.getContent())
            .likeCount(0)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .isDeleted(false)
            .build();
    }

    @Override
    public CommentResponseDto toResponseDto(Comment comment) {
        if (comment == null) {
          return null;
        }
            CommentResponseDto dto = new CommentResponseDto();
            dto.setId(comment.getId());
            dto.setContent(comment.getContent());
            dto.setUserNickname(comment.getUserNickname());
            dto.setLikeCount(comment.getLikeCount());
            dto.setCreatedAt(comment.getCreatedAt());
        return dto;
    }
}