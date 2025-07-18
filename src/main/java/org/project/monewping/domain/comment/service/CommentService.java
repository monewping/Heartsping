package org.project.monewping.domain.comment.service;

import java.util.UUID;
import org.project.monewping.domain.comment.dto.CommentResponseDto;
import org.project.monewping.domain.comment.dto.CommentRegisterRequestDto;
import org.project.monewping.global.dto.CursorPageResponse;

/**
 * 댓글 서비스 인터페이스
 * 기사에 대한 댓글 목록 조회 및 등록 기능을 정의합니다.
 */
public interface CommentService {

    // 댓글 목록 조회
    CursorPageResponse<CommentResponseDto> getComments(
        UUID articleId,
        String orderBy,
        String direction,
        String cursor,
        String after,
        int limit
    );

    // 댓글 등록
    void registerComment(CommentRegisterRequestDto requestDto);
}