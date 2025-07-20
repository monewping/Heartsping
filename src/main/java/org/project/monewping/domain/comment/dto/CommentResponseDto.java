package org.project.monewping.domain.comment.dto;

import java.time.Instant;
import java.util.UUID;


/**
 * 댓글 응답 DTO
 * 댓글 조회 API 응답에 사용됩니다.
 */
public record CommentResponseDto(
    UUID id,
    String content,
    String userNickname,
    int likeCount,
    String createdAt
) {

}