package org.project.monewping.domain.interest.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.project.monewping.domain.interest.entity.Interest;
import org.project.monewping.domain.interest.entity.Keyword;
import org.project.monewping.domain.interest.entity.QInterest;
import org.project.monewping.domain.interest.entity.QKeyword;
import org.project.monewping.domain.interest.entity.QSubscription;
import org.project.monewping.domain.interest.entity.Subscription;
import org.project.monewping.domain.user.entity.User;
import org.project.monewping.domain.user.repository.UserRepository;
import org.project.monewping.global.config.JpaAuditingConfig;
import org.project.monewping.global.config.QuerydslConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({
        JpaAuditingConfig.class,
        QuerydslConfig.class
})
@TestPropertySource(properties = "auditing.enabled=true")
@DisplayName("Interest QueryDSL 테스트")
class InterestQueryDSLTest {

    @Autowired
    private JPAQueryFactory queryFactory;

    @Autowired
    private InterestRepository interestRepository;



    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private UserRepository userRepository;

    private QInterest qInterest;
    private QKeyword qKeyword;
    private QSubscription qSubscription;
    private Interest testInterest;
    private Keyword testKeyword;
    private User testUser;
    private Subscription testSubscription;

    @BeforeEach
    void setUp() {
        qInterest = QInterest.interest;
        qKeyword = QKeyword.keyword;
        qSubscription = QSubscription.subscription;

        // 테스트 데이터 생성
        testInterest = interestRepository.save(Interest.builder()
                .name("테스트 관심사")
                .subscriberCount(10L)
                .build());

        testKeyword = new Keyword(testInterest, "테스트 키워드");
        testInterest.addKeyword(testKeyword);

        testUser = userRepository.save(User.builder()
                .email("test@example.com")
                .nickname("테스트유저")
                .password("password123")
                .isDeleted(false)
                .build());

        testSubscription = subscriptionRepository.save(new Subscription(testUser, testInterest));
    }

    @Test
    @DisplayName("QInterest를 사용한 관심사 조회 테스트")
    void testQInterestQuery() {
        // when
        List<Interest> interests = queryFactory
                .selectFrom(qInterest)
                .where(qInterest.name.contains("테스트"))
                .fetch();

        // then
        assertThat(interests).isNotEmpty();
        assertThat(interests).hasSize(1);
        assertThat(interests.get(0).getName()).isEqualTo("테스트 관심사");
    }

    @Test
    @DisplayName("QInterest를 사용한 구독자 수 기준 정렬 테스트")
    void testQInterestQueryOrderBySubscriberCount() {
        // given - 추가 관심사 생성
        Interest interest2 = interestRepository.save(Interest.builder()
                .name("테스트 관심사 2")
                .subscriberCount(20L)
                .build());

        // when
        List<Interest> interests = queryFactory
                .selectFrom(qInterest)
                .where(qInterest.name.contains("테스트"))
                .orderBy(qInterest.subscriberCount.desc())
                .fetch();

        // then
        assertThat(interests).hasSize(2);
        assertThat(interests.get(0).getSubscriberCount()).isEqualTo(20L);
        assertThat(interests.get(1).getSubscriberCount()).isEqualTo(10L);
    }

    @Test
    @DisplayName("QInterest를 사용한 구독자 수 기준 필터링 테스트")
    void testQInterestQueryBySubscriberCount() {
        // when
        List<Interest> interests = queryFactory
                .selectFrom(qInterest)
                .where(qInterest.subscriberCount.goe(5L))
                .fetch();

        // then
        assertThat(interests).isNotEmpty();
        assertThat(interests.get(0).getSubscriberCount()).isGreaterThanOrEqualTo(5L);
    }

    @Test
    @DisplayName("QKeyword를 사용한 키워드 조회 테스트")
    void testQKeywordQuery() {
        // when
        List<Keyword> keywords = queryFactory
                .selectFrom(qKeyword)
                .where(qKeyword.name.contains("테스트"))
                .fetch();

        // then
        assertThat(keywords).isNotEmpty();
        assertThat(keywords).hasSize(1);
        assertThat(keywords.get(0).getName()).isEqualTo("테스트 키워드");
    }

    @Test
    @DisplayName("QKeyword를 사용한 관심사별 키워드 조회 테스트")
    void testQKeywordQueryByInterest() {
        // when
        List<Keyword> keywords = queryFactory
                .selectFrom(qKeyword)
                .where(qKeyword.interest.id.eq(testInterest.getId()))
                .fetch();

        // then
        assertThat(keywords).isNotEmpty();
        assertThat(keywords.get(0).getInterest().getId()).isEqualTo(testInterest.getId());
    }

    @Test
    @DisplayName("QSubscription을 사용한 구독 조회 테스트")
    void testQSubscriptionQuery() {
        // when
        List<Subscription> subscriptions = queryFactory
                .selectFrom(qSubscription)
                .where(qSubscription.user.id.eq(testUser.getId()))
                .fetch();

        // then
        assertThat(subscriptions).isNotEmpty();
        assertThat(subscriptions).hasSize(1);
        assertThat(subscriptions.get(0).getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    @DisplayName("QSubscription을 사용한 관심사별 구독 조회 테스트")
    void testQSubscriptionQueryByInterest() {
        // when
        List<Subscription> subscriptions = queryFactory
                .selectFrom(qSubscription)
                .where(qSubscription.interest.id.eq(testInterest.getId()))
                .fetch();

        // then
        assertThat(subscriptions).isNotEmpty();
        assertThat(subscriptions.get(0).getInterest().getId()).isEqualTo(testInterest.getId());
    }

    @Test
    @DisplayName("QInterest와 QKeyword를 사용한 조인 쿼리 테스트")
    void testQInterestAndQKeywordJoinQuery() {
        // when
        List<Interest> interests = queryFactory
                .selectFrom(qInterest)
                .leftJoin(qKeyword).on(qInterest.id.eq(qKeyword.interest.id))
                .where(qKeyword.name.contains("테스트"))
                .fetch();

        // then
        assertThat(interests).isNotEmpty();
        assertThat(interests.get(0).getId()).isEqualTo(testInterest.getId());
    }

    @Test
    @DisplayName("QInterest와 QSubscription을 사용한 조인 쿼리 테스트")
    void testQInterestAndQSubscriptionJoinQuery() {
        // when
        List<Interest> interests = queryFactory
                .selectFrom(qInterest)
                .leftJoin(qSubscription).on(qInterest.id.eq(qSubscription.interest.id))
                .where(qSubscription.user.id.eq(testUser.getId()))
                .fetch();

        // then
        assertThat(interests).isNotEmpty();
        assertThat(interests.get(0).getId()).isEqualTo(testInterest.getId());
    }

    @Test
    @DisplayName("QInterest를 사용한 복합 조건 쿼리 테스트")
    void testQInterestComplexQuery() {
        // when
        List<Interest> interests = queryFactory
                .selectFrom(qInterest)
                .where(qInterest.name.contains("테스트")
                        .and(qInterest.subscriberCount.goe(5L)))
                .fetch();

        // then
        assertThat(interests).isNotEmpty();
        assertThat(interests.get(0).getName()).contains("테스트");
        assertThat(interests.get(0).getSubscriberCount()).isGreaterThanOrEqualTo(5L);
    }

    @Test
    @DisplayName("QInterest를 사용한 관심사 수 조회 테스트")
    void testQInterestCount() {
        // when
        Long count = queryFactory
                .select(qInterest.count())
                .from(qInterest)
                .fetchOne();

        // then
        assertThat(count).isEqualTo(1L);
    }

    @Test
    @DisplayName("QKeyword를 사용한 키워드 수 조회 테스트")
    void testQKeywordCount() {
        // when
        Long count = queryFactory
                .select(qKeyword.count())
                .from(qKeyword)
                .fetchOne();

        // then
        assertThat(count).isEqualTo(1L);
    }

    @Test
    @DisplayName("QSubscription을 사용한 구독 수 조회 테스트")
    void testQSubscriptionCount() {
        // when
        Long count = queryFactory
                .select(qSubscription.count())
                .from(qSubscription)
                .fetchOne();

        // then
        assertThat(count).isEqualTo(1L);
    }

    @Test
    @DisplayName("QInterest를 사용한 이름 검색 테스트")
    void testQInterestSearchByName() {
        // when
        List<Interest> interests = queryFactory
                .selectFrom(qInterest)
                .where(qInterest.name.containsIgnoreCase("관심사"))
                .fetch();

        // then
        assertThat(interests).isNotEmpty();
        assertThat(interests.get(0).getName()).contains("관심사");
    }

    @Test
    @DisplayName("QKeyword를 사용한 이름 검색 테스트")
    void testQKeywordSearchByName() {
        // when
        List<Keyword> keywords = queryFactory
                .selectFrom(qKeyword)
                .where(qKeyword.name.containsIgnoreCase("키워드"))
                .fetch();

        // then
        assertThat(keywords).isNotEmpty();
        assertThat(keywords.get(0).getName()).contains("키워드");
    }
} 