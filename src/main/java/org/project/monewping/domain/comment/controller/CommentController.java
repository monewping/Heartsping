package org.project.monewping.domain.comment.controller;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.project.monewping.domain.comment.dto.CommentRegisterRequestDto;
import org.project.monewping.domain.comment.dto.CommentResponseDto;
import org.project.monewping.domain.comment.service.CommentService;
import org.project.monewping.global.dto.CursorPageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 댓글 API 컨트롤러
 * 댓글 조회 및 등록 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    /**
     * 기사 ID에 해당하는 댓글 목록을 조회합니다.
     *
     * @param articleId 기사 ID (UUID)
     * @param orderBy 정렬 기준 (createdAt, likeCount)
     * @param direction 정렬 방향 (ASC, DESC)
     * @param cursor 커서 값 (선택)
     * @param after after 값 (선택)
     * @param limit 조회 개수 (기본값 50)
     * @return 커서 기반 댓글 목록 응답
     */
    @GetMapping
    public ResponseEntity<CursorPageResponse<CommentResponseDto>> getComments(
        @RequestParam UUID articleId,
        @RequestParam(required = false, defaultValue = "createdAt") String orderBy,
        @RequestParam(required = false, defaultValue = "DESC") String direction,
        @RequestParam(required = false) String cursor,
        @RequestParam(required = false) String after,
        @RequestParam(required = false, defaultValue = "50") Integer limit
    ) {
        CursorPageResponse<CommentResponseDto> response = commentService.getComments(
            articleId, orderBy, direction, cursor, after, limit
        );
        return ResponseEntity.ok(response);
    }

    /**
     * 댓글을 등록합니다.
     */
    @PostMapping
    public ResponseEntity<Void> registerComment(@RequestBody CommentRegisterRequestDto requestDto) {
      commentService.registerComment(requestDto);
      return ResponseEntity.ok().build();
    }
}