package org.project.monewping.domain.comment.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.project.monewping.domain.comment.domain.Comment;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CommentCustomRepositoryImpl implements CommentCustomRepository {

    private final EntityManager em;

    @Override
    public List<Comment> findComments(UUID articleId, String orderBy, String direction, String cursor, String after, int limit) {
        String orderColumn = getOrderColumn(orderBy);
        String sortDirection = getSortDirection(direction);

        StringBuilder sql = new StringBuilder("SELECT c FROM Comment c WHERE c.articleId = :articleId AND c.isDeleted = false");

        if (after != null) {
            sql.append(" AND c.").append(orderColumn).append(" ");
            sql.append("ASC".equalsIgnoreCase(sortDirection) ? "> :after" : "< :after");
        }

        sql.append(" ORDER BY c.").append(orderColumn).append(" ").append(sortDirection);
        sql.append(", c.id ").append(sortDirection);

        TypedQuery<Comment> query = em.createQuery(sql.toString(), Comment.class);
        query.setParameter("articleId", articleId);

        if (after != null) {
            try {
                if ("createdAt".equalsIgnoreCase(orderBy)) {
                    query.setParameter("after", Instant.parse(after));
                } else if ("likeCount".equalsIgnoreCase(orderBy)) {
                    query.setParameter("after", Integer.parseInt(after));
                }
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("after 파라미터는 ISO8601 형식이어야 합니다.", e);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("orderBy가 likeCount일 경우 after는 숫자여야 합니다.", e);
            }
        }

        return query.setMaxResults(limit + 1).getResultList();
    }

    private String getOrderColumn(String orderBy) {
        if ("createdAt".equalsIgnoreCase(orderBy)) {
            return "createdAt";
        } else if ("likeCount".equalsIgnoreCase(orderBy)) {
            return "likeCount";
        } else {
            throw new IllegalArgumentException("허용되지 않는 orderBy 값: " + orderBy);
        }
    }

    private String getSortDirection(String direction) {
        if ("ASC".equalsIgnoreCase(direction)) {
            return "ASC";
        } else if ("DESC".equalsIgnoreCase(direction)) {
            return "DESC";
        } else {
            throw new IllegalArgumentException("허용되지 않는 direction 값: " + direction);
        }
    }
}