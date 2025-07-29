package org.project.monewping.domain.notification.repository;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.project.monewping.domain.notification.entity.Notification;
import org.project.monewping.domain.notification.entity.QNotification;
import org.project.monewping.domain.user.entity.User;
import org.project.monewping.domain.user.repository.UserRepository;
import org.project.monewping.global.config.JpaAuditingConfig;
import org.project.monewping.global.config.QuerydslConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({
        JpaAuditingConfig.class,
        QuerydslConfig.class
})
@TestPropertySource(properties = "auditing.enabled=true")
@DisplayName("Notification QueryDSL 테스트")
class NotificationQueryDSLTest {

    @Autowired
    private JPAQueryFactory queryFactory;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    private QNotification qNotification;
    private User testUser;
    private Notification testNotification;

    @BeforeEach
    void setUp() {
        qNotification = QNotification.notification;

        // 테스트 데이터 생성
        testUser = userRepository.save(User.builder()
                .email("test@example.com")
                .nickname("테스트유저")
                .password("password123")
                .isDeleted(false)
                .build());

        testNotification = notificationRepository.save(new Notification(
                testUser.getId(),
                "테스트 알림 내용입니다.",
                UUID.randomUUID(),
                "ARTICLE"
        ));
    }

    @Test
    @DisplayName("QNotification 생성자 테스트")
    void testQNotificationConstructors() {
        QNotification q1 = new QNotification("notificationAlias");
        QNotification q2 = new QNotification(q1);

        PathMetadata meta = q1.getMetadata();
        QNotification q3 = new QNotification(meta);

        assertThat(q1).isNotNull();
        assertThat(q2).isNotNull();
        assertThat(q3).isNotNull();
    }

    @Test
    @DisplayName("QNotification을 사용한 알림 조회 테스트")
    void testQNotificationQuery() {
        // when
        List<Notification> notifications = queryFactory
                .selectFrom(qNotification)
                .where(qNotification.content.contains("테스트"))
                .fetch();

        // then
        assertThat(notifications).isNotEmpty();
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).getContent()).contains("테스트");
    }

    @Test
    @DisplayName("QNotification을 사용한 사용자별 알림 조회 테스트")
    void testQNotificationQueryByUser() {
        // when
        List<Notification> notifications = queryFactory
                .selectFrom(qNotification)
                .where(qNotification.userId.eq(testUser.getId()))
                .fetch();

        // then
        assertThat(notifications).isNotEmpty();
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).getUserId()).isEqualTo(testUser.getId());
    }

    @Test
    @DisplayName("QNotification을 사용한 확인되지 않은 알림 조회 테스트")
    void testQNotificationQueryUnconfirmed() {
        // when
        List<Notification> notifications = queryFactory
                .selectFrom(qNotification)
                .where(qNotification.confirmed.isFalse())
                .fetch();

        // then
        assertThat(notifications).isNotEmpty();
        assertThat(notifications.get(0).isConfirmed()).isFalse();
    }

    @Test
    @DisplayName("QNotification을 사용한 리소스 타입별 알림 조회 테스트")
    void testQNotificationQueryByResourceType() {
        // when
        List<Notification> notifications = queryFactory
                .selectFrom(qNotification)
                .where(qNotification.resourceType.eq("ARTICLE"))
                .fetch();

        // then
        assertThat(notifications).isNotEmpty();
        assertThat(notifications.get(0).getResourceType()).isEqualTo("ARTICLE");
    }

    @Test
    @DisplayName("QNotification을 사용한 생성 시간 기준 정렬 테스트")
    void testQNotificationQueryOrderByCreatedAt() {
        // given - 추가 알림 생성
        Notification notification2 = notificationRepository.save(new Notification(
                testUser.getId(),
                "테스트 알림 내용 2",
                UUID.randomUUID(),
                "ARTICLE"
        ));

        // when
        List<Notification> notifications = queryFactory
                .selectFrom(qNotification)
                .where(qNotification.userId.eq(testUser.getId()))
                .orderBy(qNotification.createdAt.desc())
                .fetch();

        // then
        assertThat(notifications).hasSize(2);
        assertThat(notifications.get(0).getCreatedAt()).isAfterOrEqualTo(notifications.get(1).getCreatedAt());
    }

    @Test
    @DisplayName("QNotification을 사용한 복합 조건 쿼리 테스트")
    void testQNotificationComplexQuery() {
        // when
        List<Notification> notifications = queryFactory
                .selectFrom(qNotification)
                .where(qNotification.userId.eq(testUser.getId())
                        .and(qNotification.resourceType.eq("ARTICLE"))
                        .and(qNotification.confirmed.isFalse()))
                .fetch();

        // then
        assertThat(notifications).isNotEmpty();
        assertThat(notifications.get(0).getUserId()).isEqualTo(testUser.getId());
        assertThat(notifications.get(0).getResourceType()).isEqualTo("ARTICLE");
        assertThat(notifications.get(0).isConfirmed()).isFalse();
    }

    @Test
    @DisplayName("QNotification을 사용한 사용자별 알림 수 조회 테스트")
    void testQNotificationCountByUser() {
        // when
        Long count = queryFactory
                .select(qNotification.count())
                .from(qNotification)
                .where(qNotification.userId.eq(testUser.getId()))
                .fetchOne();

        // then
        assertThat(count).isEqualTo(1L);
    }

    @Test
    @DisplayName("QNotification을 사용한 확인되지 않은 알림 수 조회 테스트")
    void testQNotificationCountUnconfirmed() {
        // when
        Long count = queryFactory
                .select(qNotification.count())
                .from(qNotification)
                .where(qNotification.confirmed.isFalse())
                .fetchOne();

        // then
        assertThat(count).isEqualTo(1L);
    }

    @Test
    @DisplayName("QNotification을 사용한 리소스 타입별 알림 수 조회 테스트")
    void testQNotificationCountByResourceType() {
        // when
        Long count = queryFactory
                .select(qNotification.count())
                .from(qNotification)
                .where(qNotification.resourceType.eq("ARTICLE"))
                .fetchOne();

        // then
        assertThat(count).isEqualTo(1L);
    }

    @Test
    @DisplayName("QNotification을 사용한 내용 검색 테스트")
    void testQNotificationSearchByContent() {
        // when
        List<Notification> notifications = queryFactory
                .selectFrom(qNotification)
                .where(qNotification.content.contains("내용"))
                .fetch();

        // then
        assertThat(notifications).isNotEmpty();
        assertThat(notifications.get(0).getContent()).contains("내용");
    }
} 