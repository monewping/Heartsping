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

    /**
     * 검색 조건에 따라 뉴스 기사 목록을 커서 페이지네이션으로 조회합니다.
     *
     * @param request 검색 조건 및 페이지네이션 정보
     * @return 조회된 뉴스 기사 리스트 ( limit + 1 개, 다음 페이지 존재 여부 확인용 )
     */
    @Override
    public List<Articles> searchArticles(ArticleSearchRequest request) {
        QArticles article = QArticles.articles;

        // 1. limit 값 검증 및 기본값 적용
        Integer limit = request.limit();
        if (limit == null || limit <= 0) {
            limit = 10;
        }

        BooleanBuilder builder = buildSearchPredicate(article, request);
        OrderSpecifier<?> order = buildOrderSpecifier(article, request);

        // 2. limit+1 적용
        return queryFactory
            .selectFrom(article)
            .where(builder)
            .orderBy(order)
            .limit(limit + 1)  // limit + 1 조회
            .fetch();
    }

    /**
     * 검색 조건에 맞는 뉴스 기사 총 개수를 조회합니다.
     * 커서 기준은 제외하여 정확한 전체 카운트를 반환합니다.
     *
     * @param request 검색 조건
     * @return 총 개수
     */
    @Override
    public long countArticles(ArticleSearchRequest request) {
        QArticles article = QArticles.articles;

        BooleanBuilder builder = buildSearchPredicateWithoutCursor(article, request);

        return queryFactory
            .select(article.count())
            .from(article)
            .where(builder)
            .fetchOne();
    }


    /**
     * 검색 조건에 따른 BooleanBuilder를 생성합니다.
     * 커서 페이지네이션 관련 조건 포함.
     *
     * @param article QArticles 인스턴스
     * @param request 검색 조건
     * @return BooleanBuilder 객체
     */
    private BooleanBuilder buildSearchPredicate(QArticles article, ArticleSearchRequest request) {
        BooleanBuilder builder = buildSearchPredicateWithoutCursor(article, request);

        // 커서 기반 조건 추가
        if (request.cursor() != null && !request.cursor().isBlank()) {
            UUID cursorId = null;
            try {
                cursorId = UUID.fromString(request.cursor());
            } catch (IllegalArgumentException e) {
                // cursorId = null;
            }

            if (cursorId != null) { // 여기 조건 수정
                boolean asc = "ASC".equalsIgnoreCase(request.direction());

                if ("publishDate".equalsIgnoreCase(request.orderBy())) {
                    if (request.after() != null) {
                        // 커서 조건 생성
                        BooleanBuilder cursorCondition = new BooleanBuilder();

                        if (asc) {
                            cursorCondition.or(article.publishedAt.gt(request.after()));
                            cursorCondition.or(
                                article.publishedAt.eq(request.after())
                                    .and(article.id.gt(cursorId))
                            );
                        } else {
                            cursorCondition.or(article.publishedAt.lt(request.after()));
                            cursorCondition.or(
                                article.publishedAt.eq(request.after())
                                    .and(article.id.lt(cursorId))
                            );
                        }

                        builder.and(cursorCondition);
                    } else {
                        // after 가 없을 때 단순 id 비교
                        builder.and(asc ? article.id.gt(cursorId) : article.id.lt(cursorId));
                    }
                } else {
                    // publishDate 이외 정렬 시 단순 id 비교
                    builder.and(asc ? article.id.gt(cursorId) : article.id.lt(cursorId));
                }
            }
        }

        return builder;
    }

    /**
     * 검색 조건에 따른 BooleanBuilder를 생성합니다.
     * 커서 페이지네이션 조건은 제외합니다 ( 카운트용 ).
     *
     * @param article QArticles 인스턴스
     * @param request 검색 조건
     * @return BooleanBuilder 객체
     */
    private BooleanBuilder buildSearchPredicateWithoutCursor(QArticles article, ArticleSearchRequest request) {
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

        return builder;
    }

    /**
     * 정렬 조건에 따른 OrderSpecifier를 생성합니다.
     *
     * @param article QArticles 인스턴스
     * @param request 정렬 조건 및 방향
     * @return OrderSpecifier 객체
     */
    private OrderSpecifier<?> buildOrderSpecifier(QArticles article, ArticleSearchRequest request) {
        boolean asc = "ASC".equalsIgnoreCase(request.direction());

        return switch (request.orderBy()) {
            case "commentCount" -> asc ? article.commentCount.asc() : article.commentCount.desc();
            case "viewCount" -> asc ? article.viewCount.asc() : article.viewCount.desc();
            case "publishDate" -> asc ? article.publishedAt.asc() : article.publishedAt.desc();
            default -> asc ? article.publishedAt.asc() : article.publishedAt.desc();
        };
    }
}
