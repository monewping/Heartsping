package org.project.monewping.domain.article.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.project.monewping.domain.article.entity.ArticleViews;
import org.project.monewping.domain.article.entity.Articles;
import org.project.monewping.domain.article.entity.QArticleViews;
import org.project.monewping.domain.article.entity.QArticles;
import org.project.monewping.domain.interest.entity.Interest;
import org.project.monewping.domain.interest.repository.InterestRepository;
import org.project.monewping.domain.user.entity.User;
import org.project.monewping.domain.user.repository.UserRepository;
import org.project.monewping.global.config.JpaAuditingConfig;
import org.project.monewping.global.config.QuerydslConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({
        JpaAuditingConfig.class,
        QuerydslConfig.class
})
@TestPropertySource(properties = "auditing.enabled=true")
@DisplayName("QueryDSL Q클래스 테스트")
class QueryDSLTest {

    @Autowired
    private JPAQueryFactory queryFactory;

    @Autowired
    private ArticlesRepository articlesRepository;

    @Autowired
    private ArticleViewsRepository articleViewsRepository;

    @Autowired
    private InterestRepository interestRepository;

    @Autowired
    private UserRepository userRepository;

    private QArticles qArticles;
    private QArticleViews qArticleViews;
    private Interest testInterest;
    private User testUser;
    private Articles testArticle;
    private ArticleViews testArticleView;

    @BeforeEach
    void setUp() {
        qArticles = QArticles.articles;
        qArticleViews = QArticleViews.articleViews;

        // 테스트 데이터 생성
        testInterest = interestRepository.save(Interest.builder()
                .name("테스트 관심사")
                .subscriberCount(10L)
                .build());

        testUser = userRepository.save(User.builder()
                .email("test@example.com")
                .nickname("테스트유저")
                .password("password123")
                .isDeleted(false)
                .build());

        testArticle = articlesRepository.save(Articles.builder()
                .title("테스트 기사")
                .summary("테스트 요약")
                .originalLink("https://test.com/article1")
                .source("테스트 소스")
                .publishedAt(LocalDateTime.now())
                .interest(testInterest)
                .commentCount(5L)
                .viewCount(100L)
                .deleted(false)
                .build());

        testArticleView = articleViewsRepository.save(ArticleViews.builder()
                .viewedBy(testUser.getId())
                .article(testArticle)
                .createdAt(LocalDateTime.now())
                .build());
    }

    @Test
    @DisplayName("QArticles를 사용한 기사 조회 테스트")
    void testQArticlesQuery() {
        // when
        List<Articles> articles = queryFactory
                .selectFrom(qArticles)
                .where(qArticles.title.contains("테스트"))
                .fetch();

        // then
        assertThat(articles).isNotEmpty();
        assertThat(articles).hasSize(1);
        assertThat(articles.get(0).getTitle()).isEqualTo("테스트 기사");
    }

    @Test
    @DisplayName("QArticles를 사용한 소스별 기사 조회 테스트")
    void testQArticlesQueryBySource() {
        // when
        List<Articles> articles = queryFactory
                .selectFrom(qArticles)
                .where(qArticles.source.eq("테스트 소스"))
                .fetch();

        // then
        assertThat(articles).isNotEmpty();
        assertThat(articles).hasSize(1);
        assertThat(articles.get(0).getSource()).isEqualTo("테스트 소스");
    }

    @Test
    @DisplayName("QArticles를 사용한 조회수 기준 정렬 테스트")
    void testQArticlesQueryOrderByViewCount() {
        // given - 추가 기사 생성
        Articles article2 = articlesRepository.save(Articles.builder()
                .title("테스트 기사 2")
                .summary("테스트 요약 2")
                .originalLink("https://test.com/article2")
                .source("테스트 소스")
                .publishedAt(LocalDateTime.now())
                .interest(testInterest)
                .commentCount(3L)
                .viewCount(200L)
                .deleted(false)
                .build());

        // when
        List<Articles> articles = queryFactory
                .selectFrom(qArticles)
                .where(qArticles.source.eq("테스트 소스"))
                .orderBy(qArticles.viewCount.desc())
                .fetch();

        // then
        assertThat(articles).hasSize(2);
        assertThat(articles.get(0).getViewCount()).isEqualTo(200L);
        assertThat(articles.get(1).getViewCount()).isEqualTo(100L);
    }

    @Test
    @DisplayName("QArticles를 사용한 댓글수 기준 필터링 테스트")
    void testQArticlesQueryByCommentCount() {
        // when
        List<Articles> articles = queryFactory
                .selectFrom(qArticles)
                .where(qArticles.commentCount.goe(5L))
                .fetch();

        // then
        assertThat(articles).isNotEmpty();
        assertThat(articles.get(0).getCommentCount()).isGreaterThanOrEqualTo(5L);
    }

    @Test
    @DisplayName("QArticles를 사용한 삭제되지 않은 기사 조회 테스트")
    void testQArticlesQueryNotDeleted() {
        // when
        List<Articles> articles = queryFactory
                .selectFrom(qArticles)
                .where(qArticles.deleted.isFalse())
                .fetch();

        // then
        assertThat(articles).isNotEmpty();
        assertThat(articles.get(0).isDeleted()).isFalse();
    }

    @Test
    @DisplayName("QArticleViews를 사용한 조회 기록 조회 테스트")
    void testQArticleViewsQuery() {
        // when
        List<ArticleViews> views = queryFactory
                .selectFrom(qArticleViews)
                .where(qArticleViews.viewedBy.eq(testUser.getId()))
                .fetch();

        // then
        assertThat(views).isNotEmpty();
        assertThat(views).hasSize(1);
        assertThat(views.get(0).getViewedBy()).isEqualTo(testUser.getId());
    }

    @Test
    @DisplayName("QArticleViews를 사용한 기사별 조회 기록 조회 테스트")
    void testQArticleViewsQueryByArticle() {
        // when
        List<ArticleViews> views = queryFactory
                .selectFrom(qArticleViews)
                .where(qArticleViews.article.id.eq(testArticle.getId()))
                .fetch();

        // then
        assertThat(views).isNotEmpty();
        assertThat(views).hasSize(1);
        assertThat(views.get(0).getArticle().getId()).isEqualTo(testArticle.getId());
    }

    @Test
    @DisplayName("QArticleViews를 사용한 조회 시간 기준 정렬 테스트")
    void testQArticleViewsQueryOrderByCreatedAt() {
        // given - 추가 조회 기록 생성
        ArticleViews view2 = articleViewsRepository.save(ArticleViews.builder()
                .viewedBy(testUser.getId())
                .article(testArticle)
                .createdAt(LocalDateTime.now().plusHours(1))
                .build());

        // when
        List<ArticleViews> views = queryFactory
                .selectFrom(qArticleViews)
                .where(qArticleViews.viewedBy.eq(testUser.getId()))
                .orderBy(qArticleViews.createdAt.asc())
                .fetch();

        // then
        assertThat(views).hasSize(2);
        assertThat(views.get(0).getCreatedAt()).isBefore(views.get(1).getCreatedAt());
    }

    @Test
    @DisplayName("QArticles와 QArticleViews를 사용한 조인 쿼리 테스트")
    void testQArticlesAndQArticleViewsJoinQuery() {
        // when
        List<Articles> articles = queryFactory
                .selectFrom(qArticles)
                .leftJoin(qArticleViews).on(qArticles.id.eq(qArticleViews.article.id))
                .where(qArticleViews.viewedBy.eq(testUser.getId()))
                .fetch();

        // then
        assertThat(articles).isNotEmpty();
        assertThat(articles.get(0).getId()).isEqualTo(testArticle.getId());
    }

    @Test
    @DisplayName("QArticles를 사용한 복합 조건 쿼리 테스트")
    void testQArticlesComplexQuery() {
        // when
        List<Articles> articles = queryFactory
                .selectFrom(qArticles)
                .where(qArticles.title.contains("테스트")
                        .and(qArticles.viewCount.goe(50L))
                        .and(qArticles.deleted.isFalse()))
                .fetch();

        // then
        assertThat(articles).isNotEmpty();
        assertThat(articles.get(0).getTitle()).contains("테스트");
        assertThat(articles.get(0).getViewCount()).isGreaterThanOrEqualTo(50L);
        assertThat(articles.get(0).isDeleted()).isFalse();
    }

    @Test
    @DisplayName("QArticles를 사용한 관심사별 기사 수 조회 테스트")
    void testQArticlesCountByInterest() {
        // when
        Long count = queryFactory
                .select(qArticles.count())
                .from(qArticles)
                .where(qArticles.interest.id.eq(testInterest.getId()))
                .fetchOne();

        // then
        assertThat(count).isEqualTo(1L);
    }

    @Test
    @DisplayName("QArticleViews를 사용한 사용자별 조회 수 조회 테스트")
    void testQArticleViewsCountByUser() {
        // when
        Long count = queryFactory
                .select(qArticleViews.count())
                .from(qArticleViews)
                .where(qArticleViews.viewedBy.eq(testUser.getId()))
                .fetchOne();

        // then
        assertThat(count).isEqualTo(1L);
    }
} 