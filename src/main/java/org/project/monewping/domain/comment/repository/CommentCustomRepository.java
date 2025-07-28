package org.project.monewping.domain.comment.repository;

import java.time.Instant;
import org.project.monewping.domain.comment.domain.Comment;

import java.util.List;
import java.util.UUID;
/**
 * 댓글 커스텀 레포지토리 인터페이스
 * 복합 조건에 따른 댓글 조회 기능을 제공합니다.
 */
public interface CommentCustomRepository {

    List<Comment> findComments(UUID articleId, String direction, String afterId, int limit);

    List<Comment> findCommentsByCreatedAtCursor(UUID articleId, Instant afterCreatedAt, int limit);

    List<Comment> findCommentsByLikeCountCursor(UUID articleId, Integer afterLikeCount, int limit);

}