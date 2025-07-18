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
        Instant now = Instant.now();
        return Comment.builder()
            .id(UUID.randomUUID())
            .articleId(requestDto.getArticleId())
            .userId(requestDto.getUserId())
            .userNickname("익명")  // 임시
            .content(requestDto.getContent())
            .likeCount(0)
            .createdAt(now)
            .updatedAt(now)
            .deleted(false)
            .build();
    }

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
            comment.getCreatedAt()
        );
    }
}