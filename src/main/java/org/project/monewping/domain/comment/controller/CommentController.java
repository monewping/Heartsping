package org.project.monewping.domain.comment.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.project.monewping.domain.comment.dto.CommentResponseDto;
import org.project.monewping.domain.comment.service.CommentService;
import org.project.monewping.global.dto.CursorPageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 댓글 API 컨트롤러
 * 댓글 조회 API를 제공합니다.
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
        @RequestParam(required = false, defaultValue = "createdAt")
        @Pattern(regexp = "createdAt|likeCount", message = "orderBy는 createdAt 또는 likeCount만 허용됩니다.")
        String orderBy,
        @RequestParam(required = false, defaultValue = "DESC")
        @Pattern(regexp = "ASC|DESC", message = "direction은 ASC 또는 DESC만 허용됩니다.")
        String direction,
        @RequestParam(required = false) String cursor,
        @RequestParam(required = false) String after,
        @RequestParam(required = false, defaultValue = "50")
        @Max(value = 100, message = "limit는 최대 100까지 요청 가능합니다.")
        Integer limit
    ) {
        CursorPageResponse<CommentResponseDto> response = commentService.getComments(
            articleId, orderBy, direction, cursor, after, limit
        );
        return ResponseEntity.ok(response);
    }
}