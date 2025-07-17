package org.project.monewping.domain.article.repository;

import com.fasterxml.jackson.databind.util.ArrayBuilders.BooleanBuilder;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.project.monewping.domain.article.dto.request.ArticleSearchRequest;
import org.project.monewping.domain.article.entity.Articles;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class ArticlesRepositoryImpl implements ArticlesRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Articles> searchArticles(ArticleSearchRequest request) {
        QArticles article = QArticles.articles;

        BooleanBuilder builder = new BooleanBuilder();

        if (request.keyword() != null && !request.keyword().isBlank()) {
            builder.and(article.title.containsIgnoreCase(request.keyword())
                .or(article.summary.containsIgnoreCase(request.keyword())));
        }

        if (request.interestId() != null) {
            builder.and(article.interest.id.eq(request.interestId()));
        }

        if (request.source() != null && !request.source().isBlank()) {
            builder.and(article.source.eq(request.source()));
        }

        if (request.date() != null) {
            builder.and(article.publishedAt.between(
                request.date().atStartOfDay(), request.date().atTime(23, 59, 59)
            ));
        }

        if (request.cursorId() != null) {
            builder.and(article.id.lt(request.cursorId()));
        }

        OrderSpecifier<?> order = switch (request.sortBy()) {
            case "comment" -> article.commentCount.desc();
            case "view" -> article.viewCount.desc();
            default -> article.publishedAt.desc();
        };

        return queryFactory
            .selectFrom(article)
            .where(builder)
            .orderBy(order)
            .limit(request.size() + 1)
            .fetch();
    }
}
