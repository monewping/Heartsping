package org.project.monewping.domain.notification.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.project.monewping.domain.notification.entity.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private Notification n1, n2, n3;

    @BeforeEach
    void setUp() {
        userId    = UUID.randomUUID();
        UUID resourceA = UUID.randomUUID();
        UUID resourceB = UUID.randomUUID();
        UUID resourceC = UUID.randomUUID();

        Instant t1 = Instant.parse("2025-07-15T09:00:00Z");
        Instant t2 = Instant.parse("2025-07-15T09:00:01Z");
        Instant t3 = Instant.parse("2025-07-15T09:00:02Z");

        n1 = notificationRepository.save(
            Notification.builder()
                .userId(userId)
                .content("영화와 관련된 기사가 3건 등록되었습니다.")
                .resourceId(resourceA)
                .resourceType("Article")
                .confirmed(false)
                .createdAt(t1)    // 직접 지정
                .updatedAt(t1)
                .build()
        );

        n2 = notificationRepository.save(
            Notification.builder()
                .userId(userId)
                .content("축구와 관련된 기사가 1건 등록되었습니다.")
                .resourceId(resourceB)
                .resourceType("Article")
                .confirmed(false)
                .createdAt(t2)
                .updatedAt(t2)
                .build()
        );

        n3 = notificationRepository.save(
            Notification.builder()
                .userId(userId)
                .content("Binu님이 나의 댓글을 좋아합니다.")
                .resourceId(resourceC)
                .resourceType("Comment")
                .confirmed(false)
                .createdAt(t3)
                .updatedAt(t3)
                .build()
        );

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
    @DisplayName("첫 페이지 조회: 커서 없이 limit=2이면 가장 오래된 2개만 반환된다")
    void testFindPageFirstPage() {
        // given
        Pageable pageable = PageRequest.of(0, 2);

        // when
        List<Notification> page = notificationRepository.findPage(
            userId, null, null, pageable
        );

        // then
        assertThat(page)
            .as("첫 페이지는 가장 오래된 알림 2개")
            .hasSize(2);
    }

    @Test
    @DisplayName("다음 페이지 조회: after=n2.createdAt, lastId=n2.id 부터 이어서 반환된다")
    void testFindPageNextPage() {
        // given
        Pageable firstPageable = PageRequest.of(0, 2);
        List<Notification> firstPage = notificationRepository.findPage(
            userId, null, null, firstPageable
        );
        Notification lastOfFirst = firstPage.get(1);
        Instant after = lastOfFirst.getCreatedAt();
        UUID lastId = lastOfFirst.getId();

        // when
        Pageable secondPageable = PageRequest.of(0, 2);
        List<Notification> secondPage = notificationRepository.findPage(
            userId, after, lastId, secondPageable
        );

        // then
        assertThat(secondPage)
            .as("반환된 모든 알림의 createdAt 은 after 시각 이상이어야 한다")
            .allMatch(n ->
                !n.getCreatedAt().isBefore(after)
            );
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
}
