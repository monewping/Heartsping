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
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@EnableJpaAuditing
@DisplayName("Notification Repository 슬라이스 테스트")
public class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    private UUID userId;
    private UUID resourceA;
    private UUID resourceB;

    @BeforeEach
    void setUp() throws InterruptedException {
        notificationRepository.deleteAll();
        userId    = UUID.randomUUID();
        resourceA = UUID.randomUUID();
        resourceB = UUID.randomUUID();

        notificationRepository.save(new Notification(userId, "영화와 관련된 기사가 3건 등록되었습니다.", resourceA, "Article"));
        Thread.sleep(5);
        notificationRepository.save(new Notification(userId, "축구와 관련된 기사가 1건 등록되었습니다.", resourceA, "Article"));
        Thread.sleep(5);
        notificationRepository.save(new Notification(userId, "Binu님이 나의 댓글을 좋아합니다.", resourceB, "Comment"));
    }

    @Test
    @DisplayName("특정 사용자의 확인하지 않은 알림 개수 반환 성공")
    void countByUserIdAndConfirmedFalse() {
        long count = notificationRepository.countByUserIdAndConfirmedFalse(userId);
        assertThat(count).isEqualTo(3);
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
        Pageable pageable = PageRequest.of(
            0,
            2,
            Sort.by("createdAt").ascending().and(Sort.by("id").ascending())
        );

        List<Notification> firstPage = notificationRepository.findPageFirst(userId, pageable);
        assertThat(firstPage).hasSize(2);

        Instant cursor = firstPage.get(1).getCreatedAt();

        List<Notification> nextPage = notificationRepository.findPageAfter(userId, cursor, pageable);

        assertThat(nextPage).allMatch(n -> n.getCreatedAt().isAfter(cursor));
        assertThat(nextPage).hasSize(1);
    }
}
