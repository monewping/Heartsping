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
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
@Import(JpaAuditingConfig.class)
@DisplayName("Notification Repository 슬라이스 테스트")
public class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    private UUID userId;
    private UUID resourceA;
    private UUID resourceB;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        userId    = UUID.randomUUID();
        resourceA = UUID.randomUUID();
        resourceB = UUID.randomUUID();

        notificationRepository.save(new Notification(userId, "영화와 관련된 기사가 3건 등록되었습니다.", resourceA, "Article"));
        notificationRepository.save(new Notification(userId, "축구와 관련된 기사가 1건 등록되었습니다.", resourceA, "Article"));
        notificationRepository.save(new Notification(userId, "Binu님이 나의 댓글을 좋아합니다.", resourceB, "Comment"));
    }

    @Test
    @DisplayName("특정 사용자의 확인하지 않은 알림 개수 반환 성공")
    void testCountByUserIdAndConfirmedFalse() {
        long count = notificationRepository.countByUserIdAndConfirmedFalse(userId);
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("알림 목록 조회 기능의 cursor를 이용한 페이징 처리 성공")
    void testFindPageSlice() {
        List<Notification> firstPage = notificationRepository.findPageSlice(
            userId,
            null,
            PageRequest.of(0, 2)
        );
        assertThat(firstPage).hasSize(2);

        Instant cursor = firstPage.get(1).getCreatedAt();
        List<Notification> secondPage = notificationRepository.findPageSlice(
            userId,
            cursor,
            PageRequest.of(0, 2)
        );
        assertThat(secondPage).allMatch(n -> n.getCreatedAt().isAfter(cursor));
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
