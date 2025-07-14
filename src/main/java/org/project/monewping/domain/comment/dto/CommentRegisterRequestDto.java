package org.project.monewping.domain.comment.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

/**
 * 댓글 등록 요청 DTO
 */
@Getter
@Setter
public class CommentRegisterRequestDto {
    private UUID articleId;
    private UUID userId;
    private String content;
}