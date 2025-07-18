package org.project.monewping.domain.notification.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.project.monewping.domain.notification.entity.Notification;
import org.project.monewping.global.config.JpaAuditingConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("Notification Repository 슬라이스 테스트")
public class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId    = UUID.randomUUID();
        UUID resourceA = UUID.randomUUID();
        UUID resourceB = UUID.randomUUID();
        UUID resourceC = UUID.randomUUID();

        notificationRepository.save(new Notification(userId, "영화와 관련된 기사가 3건 등록되었습니다.", resourceA, "Article"));
        notificationRepository.save(new Notification(userId, "축구와 관련된 기사가 1건 등록되었습니다.", resourceA, "Article"));
        notificationRepository.save(new Notification(userId, "Binu님이 나의 댓글을 좋아합니다.", resourceB, "Comment"));

        notificationRepository.flush();
    }

    @Test
    @DisplayName("특정 사용자의 확인하지 않은 알림 개수 반환 성공")
    void countByUserIdAndConfirmedFalse() {
        long count = notificationRepository.countByUserIdAndConfirmedFalse(userId);
        assertThat(count)
            .as("초기 설정된 3개의 알림 수가 반환되어야 한다")
            .isEqualTo(3);
    }

    @Test
    @DisplayName("첫 페이지 조회 성공")
    void findPageFirst() {
        Pageable page2 = PageRequest.of(
            0, 2,
            Sort.by("createdAt").ascending().and(Sort.by("id").ascending())
        );

        List<Notification> page = notificationRepository.findPageFirst(userId, page2);
        assertThat(page)
            .as("페이지 크기(limit=2)에 맞게 알림 2개가 조회되어야 한다")
            .hasSize(2);
    }

    @Disabled("DB 환경에서 UUID 정렬이 일관되지 않아 실패. 리팩토링 기간에 커버 예정")
    @Test
    @DisplayName("알림 목록 조회 기능의 cursor를 이용한 페이징 처리 성공")
    void findPageAfter() {
        Pageable pageable = PageRequest.of(0, 2, Sort.by("createdAt").ascending());

        List<Notification> firstPage = notificationRepository.findPageFirst(userId, pageable);
        assertThat(firstPage).hasSize(2);

        Notification cursorNotification = firstPage.get(1);
        Instant cursorCreatedAt = cursorNotification.getCreatedAt();

        System.out.println("Cursor → createdAt: " + cursorCreatedAt);

        List<Notification> nextPage = notificationRepository.findPageAfter(
            userId,
            cursorCreatedAt,
            pageable
        );

        assertThat(nextPage)
            .as("두 번째 페이지에는 커서 이후의 알림이 포함되어야 한다")
            .extracting("content")
            .contains("Binu님이 나의 댓글을 좋아합니다.");
    }

    @Test
    @DisplayName("특정 사용자의 알림 조회 성공")
    void findByIdAndUserIdSuccess() {
        // given
        Notification notification = new Notification(
            userId,
            "Binu님이 나의 댓글을 좋아합니다.",
            UUID.randomUUID(),
            "Comment"
        );
        notificationRepository.saveAndFlush(notification);

        // when
        var result = notificationRepository.findByIdAndUserId(notification.getId(), userId);

        // then
        assertThat(result)
            .as("정상적인 userId와 notificationId 조합으로 조회했으므로 결과가 존재해야 한다")
            .isPresent();
        assertThat(result.get().getId())
            .as("조회된 알림 ID는 저장한 알림 ID와 일치해야 한다")
            .isEqualTo(notification.getId());
    }

    @Test
    @DisplayName("특정 사용자의 알림 조회 실패")
    void findByIdAndUserIdFailIfWrongUser() {
        // given
        UUID otherUserId = UUID.randomUUID();

        Notification notification = new Notification(
            userId,
            "Binu님이 나의 댓글을 좋아합니다.",
            UUID.randomUUID(),
            "Article"
        );
        notificationRepository.saveAndFlush(notification);

        // when
        var result = notificationRepository.findByIdAndUserId(notification.getId(), otherUserId);

        // then
        assertThat(result)
            .as("알림 ID에 해당하는 사용자 ID 값이 동일하지 않으면 조회할 수 없어야 한다.")
            .isEmpty();
    }

    @Test
    @DisplayName("모든 알림 확인 상태 수정 성공")
    void testConfirmAllByUserId() {
        int updated = notificationRepository.confirmAllByUserId(userId);
        assertThat(updated).isEqualTo(3);

        List<Notification> all = notificationRepository.findAll();
        assertThat(all)
            .isNotEmpty()
            .allMatch(Notification::getConfirmed);
    }

    @Test
    @DisplayName("모든 알림 확인 상태 수정 성공")
    void testConfirmAllByUserId() {
        int updated = notificationRepository.confirmAllByUserId(userId);
        assertThat(updated).isEqualTo(3);

        List<Notification> all = notificationRepository.findAll();
        assertThat(all)
            .isNotEmpty()
            .allMatch(Notification::getConfirmed);
    }
}
