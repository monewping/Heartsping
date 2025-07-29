package org.project.monewping.domain.comment.dto;

import java.util.UUID;

/**

 댓글 응답 DTO
 댓글 조회 API 응답에 사용됩니다.*/
public record CommentResponseDto(
    UUID id,
    UUID articleId,
    UUID userId,
    String userNickname,
    String content,
    int likeCount,
    boolean likedByMe,
    String createdAt
) {

}