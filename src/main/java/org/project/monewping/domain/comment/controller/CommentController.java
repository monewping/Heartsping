package org.project.monewping.domain.comment.controller;

import lombok.RequiredArgsConstructor;
import org.project.monewping.domain.comment.dto.CommentResponseDto;
import org.project.monewping.domain.comment.service.CommentService;
import org.project.monewping.global.dto.CursorPageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 댓글 API 컨트롤러
 * 댓글 조회 API를 제공한다.
 */
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    /**
     * 댓글 목록 조회 API (커서 기반)
     *
     * @param cursorId 커서 ID (null이면 최신부터 조회)
     * @param size 조회할 댓글 개수 (기본값 10)
     * @return 댓글 목록 응답
     */
    @GetMapping
    public ResponseEntity<CursorPageResponse<CommentResponseDto>> getComments(
        @RequestParam(required = false) Long cursorId,
        @RequestParam(defaultValue = "10") int size) {
      CursorPageResponse<CommentResponseDto> response = commentService.getComments(cursorId, size);
      return ResponseEntity.ok(response);
    }
}