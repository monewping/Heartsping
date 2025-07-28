package org.project.monewping.domain.comment.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 댓글 수정 요청 DTO.
 * <p>
 * 댓글 수정 시 사용자가 입력한 내용을 전달하기 위한 객체입니다.
 * <p>
 * 필드:
 * - content: 수정할 댓글 내용 (필수)
 *
 * @param content 수정할 댓글 내용
 */
public record CommentUpdateRequestDto(
    @NotBlank(message = "댓글 내용은 필수입니다.")
    String content
) {

}