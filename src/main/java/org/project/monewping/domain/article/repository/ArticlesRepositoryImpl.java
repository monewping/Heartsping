package org.project.monewping.domain.article.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
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
            builder.and(
                article.title.containsIgnoreCase(request.keyword())
                    .or(article.summary.containsIgnoreCase(request.keyword()))
            );
        }

        // 관심사 필터
        if (request.interestId() != null) {
            builder.and(article.interest.id.eq(request.interestId()));
        }

        // 출처 필터 (sourceIn 리스트 처리)
        if (request.sourceIn() != null && !request.sourceIn().isEmpty()) {
            builder.and(article.source.in(request.sourceIn()));
        }

        // 날짜 범위 필터
        if (request.publishDateFrom() != null && request.publishDateTo() != null) {
            builder.and(article.publishedAt.between(request.publishDateFrom(), request.publishDateTo()));
        } else if (request.publishDateFrom() != null) {
            builder.and(article.publishedAt.goe(request.publishDateFrom()));
        } else if (request.publishDateTo() != null) {
            builder.and(article.publishedAt.loe(request.publishDateTo()));
        }

        // 커서 페이지네이션 - cursor: ID 기반, after: publishDate 기반 보조 커서
        if (request.cursor() != null) {
            // 커서가 UUID 문자열로 주어진 경우를 가정
            UUID cursorId = UUID.fromString(request.cursor());

            // 정렬 방향에 따라 ID 기준 커서 조건 다르게 처리
            boolean asc = "ASC".equalsIgnoreCase(request.direction());

            if ("publishDate".equalsIgnoreCase(request.orderBy())) {
                if (asc) {
                    // publishDate 오름차순일 때 커서 이후 데이터 (publishDate > after or id > cursorId)
                    if (request.after() != null) {
                        builder.and(
                            article.publishedAt.gt(request.after())
                                .or(
                                    article.publishedAt.eq(request.after())
                                        .and(article.id.gt(cursorId))
                                )
                        );
                    } else {
                        builder.and(article.id.gt(cursorId));
                    }
                } else {
                    // 내림차순일 때 (publishDate < after or id < cursorId)
                    if (request.after() != null) {
                        builder.and(
                            article.publishedAt.lt(request.after())
                                .or(article.publishedAt.eq(request.after()).and(article.id.lt(cursorId)))
                        );
                    } else {
                        builder.and(article.id.lt(cursorId));
                    }
                }
            } else {
                // publishDate 외의 정렬은 ID 기준 커서만 사용
                if (asc) {
                    builder.and(article.id.gt(cursorId));
                } else {
                    builder.and(article.id.lt(cursorId));
                }
            }
        }

        // 정렬 조건
        OrderSpecifier<?> order = switch (request.orderBy()) {
            case "commentCount" -> "ASC".equalsIgnoreCase(request.direction()) ?
                article.commentCount.asc() : article.commentCount.desc();

            case "viewCount" -> "ASC".equalsIgnoreCase(request.direction()) ?
                article.viewCount.asc() : article.viewCount.desc();

            case "publishDate" -> "ASC".equalsIgnoreCase(request.direction()) ?
                article.publishedAt.asc() : article.publishedAt.desc();

            default -> "ASC".equalsIgnoreCase(request.direction()) ?
                article.publishedAt.asc() : article.publishedAt.desc();
        };

        return queryFactory
            .selectFrom(article)
            .where(builder)
            .orderBy(order)
            .limit(request.limit() + 1)  // 페이징 +1개 조회로 다음 페이지 여부 판단
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
