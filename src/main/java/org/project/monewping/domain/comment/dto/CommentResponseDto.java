package org.project.monewping.domain.comment.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 댓글 응답 DTO
 * 댓글 조회 API 응답에 사용됩니다.
 */
@Getter
@AllArgsConstructor
public class CommentResponseDto {
    private Long id;
    private String content;
    private String nickname;
    private int likeCount;
    private LocalDateTime createdAt;
}