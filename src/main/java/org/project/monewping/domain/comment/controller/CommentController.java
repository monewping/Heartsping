package org.project.monewping.domain.comment.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.monewping.domain.comment.dto.CommentRegisterRequestDto;
import org.project.monewping.domain.comment.dto.CommentResponseDto;
import org.project.monewping.domain.comment.dto.CommentUpdateRequestDto;
import org.project.monewping.domain.comment.service.CommentService;
import org.project.monewping.global.dto.CursorPageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 댓글 API 컨트롤러
 * 댓글 조회 및 등록 API를 제공합니다.
 */
@Slf4j
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
     * @param afterId afterId 값 (선택, UUID)
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
        @RequestParam(required = false) String afterId,
        @RequestParam(required = false, defaultValue = "50") Integer limit
    ) {
        CursorPageResponse<CommentResponseDto> response = commentService.getComments(
            articleId, orderBy, direction, cursor, after, afterId, limit
        );
        log.info("[CommentController] 댓글 목록 조회 완료 - articleId: {}, count: {}", articleId, response.size());
        return ResponseEntity.ok(response);
    }

    /**
     * 댓글을 등록합니다.
     */
    @PostMapping
    public ResponseEntity<Void> registerComment(@RequestBody @Valid CommentRegisterRequestDto requestDto) {
        commentService.registerComment(requestDto);
        return ResponseEntity.status(201).build();
    }

    /**
     * 댓글을 삭제합니다.
     * 논리 삭제와 물리 삭제로 나뉜다.
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
        @PathVariable UUID commentId,
        @RequestParam UUID userId
    ) {
        commentService.deleteComment(commentId, userId);
        log.info("[CommentController] 댓글 논리 삭제 완료 - commentId: {}, userId: {}", commentId, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{commentId}/hard")
    public ResponseEntity<Void> deleteCommentPhysically(
        @PathVariable UUID commentId,
        @RequestParam UUID userId
    ) {
        commentService.deleteCommentPhysically(commentId, userId);
        log.info("[CommentController] 댓글 물리 삭제 완료 - commentId: {}, userId: {}", commentId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 댓글을 수정합니다.
     *
     * @param commentId 댓글 ID (UUID)
     * @param request 댓글 수정 요청 DTO
     * @return HTTP 200 OK 응답
     */
    @PatchMapping("/{commentId}")
    public ResponseEntity<Void> updateComment(
        @PathVariable UUID commentId,
        @RequestParam UUID userId, // 본인 확인을 위해 userId 추가
        @RequestBody @Valid CommentUpdateRequestDto request
    ) {
        commentService.updateComment(commentId, userId, request);
        return ResponseEntity.ok().build();
    }
}