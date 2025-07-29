package org.project.monewping.domain.comment.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.project.monewping.domain.comment.entity.Comment;
import org.project.monewping.domain.comment.entity.QComment;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CommentCustomRepositoryImpl implements CommentCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Comment> findComments(UUID articleId, String direction, String afterId, int limit) {
        QComment comment = QComment.comment;

        BooleanBuilder predicate = new BooleanBuilder();
        predicate.and(comment.articleId.eq(articleId));

        if (afterId != null && !afterId.isBlank()) {
            UUID cursorUuid = UUID.fromString(afterId);
            if ("desc".equalsIgnoreCase(direction)) {
                predicate.and(comment.id.lt(cursorUuid));
            } else {
                predicate.and(comment.id.gt(cursorUuid));
            }
        }

        OrderSpecifier<UUID> order = "desc".equalsIgnoreCase(direction) ? comment.id.desc() : comment.id.asc();

        return queryFactory
            .selectFrom(comment)
            .where(predicate)
            .orderBy(order)
            .limit(limit)
            .fetch();
    }

    @Override
    public List<Comment> findCommentsByCreatedAtCursor(UUID articleId, Instant afterCreatedAt,
        int limit) {
        QComment comment = QComment.comment;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(comment.articleId.eq(articleId));

        if (afterCreatedAt != null) {
            // 내림차순 정렬이므로 afterCreatedAt보다 작거나 같은 조건 (더 최신 댓글들 조회)
            builder.and(comment.createdAt.lt(afterCreatedAt));
        }

        return queryFactory
            .selectFrom(comment)
            .where(builder)
            .orderBy(comment.createdAt.desc(), comment.id.desc()) // 보조 정렬 필드로 id 사용
            .limit(limit)
            .fetch();
    }

    @Override
    public List<Comment> findCommentsByLikeCountCursor(UUID articleId, Integer afterLikeCount,
        int limit) {
        QComment comment = QComment.comment;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(comment.articleId.eq(articleId));

        if (afterLikeCount != null) {
            builder.and(comment.likeCount.lt(afterLikeCount));
        }

        return queryFactory
            .selectFrom(comment)
            .where(builder)
            .orderBy(comment.likeCount.desc(), comment.id.desc()) // 보조 정렬로 id 내림차순
            .limit(limit)
            .fetch();
    }
}