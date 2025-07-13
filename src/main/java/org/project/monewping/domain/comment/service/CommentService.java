package org.project.monewping.domain.comment.service;

import org.project.monewping.domain.comment.dto.CommentResponseDto;
import org.project.monewping.global.dto.CursorPageResponse;

/**
 * 댓글 서비스 인터페이스
 * 댓글 도메인 관련 비즈니스 로직을 정의한다.
 */
public interface CommentService {
    /**
     * 댓글 목록 조회 (커서 기반)
     *
     * @param cursorId 커서 ID
     * @param size 페이지 크기
     * @return 댓글 목록 응답 DTO
     */
    CursorPageResponse<CommentResponseDto> getComments(Long cursorId, int size);
}