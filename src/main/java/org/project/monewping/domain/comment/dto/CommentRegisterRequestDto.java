package org.project.monewping.domain.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

/**
 * 댓글 등록 요청 DTO
 */
@Getter
@Setter
public class CommentRegisterRequestDto {

    @NotNull(message = "articleId는 필수입니다.")
    private UUID articleId;

    @NotNull(message = "userId는 필수입니다.")
    private UUID userId;

    @NotBlank(message = "userNickname은 필수입니다.")
    private String userNickname;

    @NotBlank(message = "내용은 비어 있을 수 없습니다.")
    private String content;
}