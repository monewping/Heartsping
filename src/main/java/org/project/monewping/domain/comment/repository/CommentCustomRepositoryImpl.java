package org.project.monewping.domain.comment.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.project.monewping.domain.comment.domain.Comment;
import org.springframework.stereotype.Repository;

/**
 * 댓글 커스텀 레포지토리 구현체.
 * EntityManager를 이용하여 동적 JPQL로 커서 기반 댓글 목록 조회 기능을 제공합니다.
 */
@Repository
@RequiredArgsConstructor
public class CommentCustomRepositoryImpl implements CommentCustomRepository {

    private final EntityManager em;
    /**
     * 기사 ID 및 커서 기반으로 댓글 목록을 조회합니다.
     * 정렬 기준과 방향에 따라 커서 기반 페이지네이션을 수행하며,
     * limit 개수만큼 댓글을 조회합니다.
     */
    @Override
    public List<Comment> findComments(UUID articleId, String orderBy, String direction,
        String cursor, String after, int limit) {
        // 허용된 정렬 기준과 방향을 제한
        List<String> allowedOrderBy = List.of("createdAt", "likeCount");
        List<String> allowedDirection = List.of("ASC", "DESC");

        if (!allowedOrderBy.contains(orderBy)) {
            throw new IllegalArgumentException("허용되지 않은 orderBy 값입니다.");
        }
        if (!allowedDirection.contains(direction.toUpperCase())) {
            throw new IllegalArgumentException("허용되지 않은 direction 값입니다.");
        }

        // 논리 삭제되지 않은 댓글만 조회
        StringBuilder sql = new StringBuilder(
            "SELECT c FROM Comment c WHERE c.articleId = :articleId AND c.deleted = false");

        if (after != null) {
            sql.append(" AND c.createdAt ");
            sql.append("ASC".equalsIgnoreCase(direction) ? "> :after" : "< :after");
        }

        sql.append(" ORDER BY c.").append(orderBy).append(" ").append(direction.toUpperCase());
        sql.append(", c.id ").append(direction.toUpperCase());

        TypedQuery<Comment> query = em.createQuery(sql.toString(), Comment.class);
        query.setParameter("articleId", articleId);

        if (after != null) {
            query.setParameter("after",
                LocalDateTime.parse(after)); // after는 ISO8601 DateTime 문자열이어야 함.
        }

        return query.setMaxResults(limit + 1).getResultList();
    }
}