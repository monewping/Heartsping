package org.project.monewping.domain.article.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.project.monewping.domain.article.dto.request.ArticleSearchRequest;
import org.project.monewping.domain.article.entity.Articles;
import org.project.monewping.domain.article.entity.QArticles;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class ArticlesRepositoryImpl implements ArticlesRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Articles> searchArticles(ArticleSearchRequest request) {
        QArticles article = QArticles.articles;

        BooleanBuilder builder = new BooleanBuilder();

        // 키워드: 제목 or 요약 부분일치
        if (request.keyword() != null && !request.keyword().isBlank()) {
            builder.and(article.title.containsIgnoreCase(request.keyword())
                .or(article.summary.containsIgnoreCase(request.keyword())));
        }

        if (request.interestId() != null) {
            builder.and(article.interest.id.eq(request.interestId()));
        }

        if (request.sourceIn() != null && !request.sourceIn().isEmpty()) {
            builder.and(article.source.in(request.sourceIn()));
        }

        if (request.publishDateFrom() != null) {
            builder.and(article.publishedAt.goe(request.publishDateFrom()));
        }

        if (request.publishDateTo() != null) {
            builder.and(article.publishedAt.loe(request.publishDateTo()));
        }

        // 정렬 기준 및 커서 조건 처리
        OrderSpecifier<?> orderSpecifier;
        if ("viewCount".equals(request.orderBy())) {
            orderSpecifier = "DESC".equalsIgnoreCase(request.direction())
                ? article.viewCount.desc() : article.viewCount.asc();
        } else if ("commentCount".equals(request.orderBy())) {
            orderSpecifier = "DESC".equalsIgnoreCase(request.direction())
                ? article.commentCount.desc() : article.commentCount.asc();
        } else if ("publishDate".equals(request.orderBy()) || request.orderBy() == null) {
            orderSpecifier = "DESC".equalsIgnoreCase(request.direction())
                ? article.publishedAt.desc() : article.publishedAt.asc();
        } else {
            throw new IllegalArgumentException("지원하지 않는 정렬 기준입니다: " + request.orderBy());
        }

        // 커서 기반 조건 (after 또는 cursor 기준)
        if (request.cursor() != null) {
            UUID cursorId = UUID.fromString(request.cursor());
            LocalDateTime cursorPublishedAt = request.after();

            // fallback: after가 없으면 DB에서 조회하여 publishedAt 사용
            if (cursorPublishedAt == null) {
                Articles cursorArticle = queryFactory
                    .selectFrom(article)
                    .where(article.id.eq(cursorId))
                    .fetchOne();

                if (cursorArticle == null) {
                    throw new IllegalArgumentException("유효하지 않은 커서 ID입니다: " + cursorId);
                }
                cursorPublishedAt = cursorArticle.getPublishedAt();
            }

            if ("DESC".equalsIgnoreCase(request.direction())) {
                builder.and(
                    article.publishedAt.lt(cursorPublishedAt)
                        .or(article.publishedAt.eq(cursorPublishedAt).and(article.id.lt(cursorId)))
                );
            } else {
                builder.and(
                    article.publishedAt.gt(cursorPublishedAt)
                        .or(article.publishedAt.eq(cursorPublishedAt).and(article.id.gt(cursorId)))
                );
            }
        }

        return queryFactory
            .selectFrom(article)
            .where(builder)
            .orderBy(orderSpecifier, article.id.desc()) // 보조 정렬 필요 시 유지
            .limit(request.limit() + 1)
            .fetch();
    }

    @Override
    public long countArticles(ArticleSearchRequest request) {
        QArticles article = QArticles.articles;

        BooleanBuilder builder = new BooleanBuilder();

        if (request.keyword() != null && !request.keyword().isBlank()) {
            builder.and(
                article.title.containsIgnoreCase(request.keyword())
                    .or(article.summary.containsIgnoreCase(request.keyword()))
            );
        }

        if (request.interestId() != null) {
            builder.and(article.interest.id.eq(request.interestId()));
        }

        if (request.sourceIn() != null && !request.sourceIn().isEmpty()) {
            builder.and(article.source.in(request.sourceIn()));
        }

        if (request.publishDateFrom() != null && request.publishDateTo() != null) {
            builder.and(article.publishedAt.between(request.publishDateFrom(), request.publishDateTo()));
        } else if (request.publishDateFrom() != null) {
            builder.and(article.publishedAt.goe(request.publishDateFrom()));
        } else if (request.publishDateTo() != null) {
            builder.and(article.publishedAt.loe(request.publishDateTo()));
        }

        // 커서 조건은 count 쿼리에서는 제외
        return queryFactory
            .select(article.count())
            .from(article)
            .where(builder)
            .fetchOne();
    }
}
