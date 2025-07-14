package org.project.monewping.domain.comment.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 댓글 응답 DTO
 * 댓글 조회 API 응답에 사용됩니다.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponseDto {
    private UUID id;
    private String content;
    private String userNickname;
    private int likeCount;
    private LocalDateTime createdAt;
}