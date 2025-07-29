package org.project.monewping.domain.comment.controller;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.project.monewping.domain.comment.service.CommentLikeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 댓글 좋아요 API 컨트롤러
 * 댓글에 대한 좋아요 등록 및 취소 기능을 제공합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments")
public class CommentLikeController {
    private final CommentLikeService commentLikeService;
        /**
        * 댓글 좋아요 등록
        */
        @PostMapping("/{commentId}/comment-likes")
        public ResponseEntity<Void> likeComment(
            @RequestHeader("Monew-Request-User-Id") UUID userId,
            @PathVariable UUID commentId) {
          commentLikeService.likeComment(userId, commentId);
          return ResponseEntity.ok().build();
        }

         /**
         * * 댓글 좋아요 취소
         */
         @DeleteMapping("/{commentId}/comment-likes")
         public ResponseEntity<Void> unlikeComment(
             @RequestHeader("Monew-Request-User-Id") UUID userId,
             @PathVariable UUID commentId) {
           commentLikeService.unlikeComment(userId, commentId);
           return ResponseEntity.ok().build();
         }
}