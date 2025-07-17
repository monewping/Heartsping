package org.project.monewping.domain.notification.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.project.monewping.domain.notification.entity.Notification;
import org.project.monewping.global.config.JpaAuditingConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@Transactional
@ActiveProfiles("test")
@Import(JpaAuditingConfig.class)  // 추가
@EntityScan(basePackages = "org.project.monewping.domain.notification.entity")  // 추가
@EnableJpaRepositories(basePackages = "org.project.monewping.domain.notification.repository")  // 추가
@DisplayName("Notification Repository 슬라이스 테스트")
public class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    private UUID userId;

    @BeforeEach
    void setUp() throws InterruptedException {
        userId    = UUID.randomUUID();
        UUID resourceA = UUID.randomUUID();
        UUID resourceB = UUID.randomUUID();
        UUID resourceC = UUID.randomUUID();

        notificationRepository.save(Notification.ofForTest(
            userId, "영화와 관련된 기사가 3건 등록되었습니다.", resourceA, "Article", Instant.parse("2025-06-30T00:00:00Z")));
        notificationRepository.save(Notification.ofForTest(
            userId, "축구와 관련된 기사가 1건 등록되었습니다.", resourceA, "Article", Instant.parse("2025-07-03T00:00:10Z")));
        notificationRepository.save(Notification.ofForTest(
            userId, "Binu님이 나의 댓글을 좋아합니다.", resourceB, "Comment", Instant.parse("2025-07-04T00:00:20Z")));
        notificationRepository.save(Notification.ofForTest(
            userId, "새로운 관심사가 등록되었습니다.", resourceC, "Interest", Instant.parse("2025-07-05T00:00:30Z")));

        notificationRepository.flush();
    }

    @Test
    @DisplayName("특정 사용자의 확인하지 않은 알림 개수 반환 성공")
    void countByUserIdAndConfirmedFalse() {
        long count = notificationRepository.countByUserIdAndConfirmedFalse(userId);
        assertThat(count).isEqualTo(4);
    }

    @Test
    @DisplayName("첫 페이지 조회 성공")
    void findPageFirst() {
        Pageable page2 = PageRequest.of(
            0, 2,
            Sort.by("createdAt").ascending().and(Sort.by("id").ascending())
        );

        List<Notification> page = notificationRepository.findPageFirst(userId, page2);
        assertThat(page).hasSize(2);
    }

    @Test
    @DisplayName("알림 목록 조회 기능의 cursor를 이용한 페이징 처리 성공")
    void findPageAfter() {
        // 정렬 기준을 일치시킴 (Repository의 실제 정렬 기준과 동일하게)
        Pageable pageable = PageRequest.of(0, 2, 
            Sort.by("createdAt").ascending().and(Sort.by("id").ascending()));

        List<Notification> firstPage = notificationRepository.findPageFirst(userId, pageable);
        assertThat(firstPage).hasSize(2);

        // 첫 페이지의 마지막 알림을 커서로 사용
        Notification cursorNotification = firstPage.get(1);
        Instant cursorCreatedAt = cursorNotification.getCreatedAt();

        System.out.println("Cursor → createdAt: " + cursorCreatedAt);

        List<Notification> nextPage = notificationRepository.findPageAfter(
            userId,
            cursorCreatedAt,
            pageable
        );

        // 다음 페이지에 "Binu님이 나의 댓글을 좋아합니다."가 포함되어야 함
        assertThat(nextPage).extracting("content").contains("Binu님이 나의 댓글을 좋아합니다.");
    }
}
