package org.project.monewping.domain.comment.repository;

import org.project.monewping.domain.comment.domain.Comment;

import java.util.List;
import java.util.UUID;
/**
 * 댓글 커스텀 레포지토리 인터페이스
 * 복합 조건에 따른 댓글 조회 기능을 제공합니다.
 */
public interface CommentCustomRepository {
    List<Comment> findComments(UUID articleId, String orderBy, String direction, String cursor, String after, int limit);
}