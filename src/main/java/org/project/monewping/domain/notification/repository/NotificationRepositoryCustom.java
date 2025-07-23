package org.project.monewping.domain.notification.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.project.monewping.domain.notification.entity.Notification;
import org.springframework.data.domain.Pageable;

public interface NotificationRepositoryCustom {
    /**
     * 특정 사용자의 미확인 알림을 커서(cursor) 기반으로 페이지 조회합니다.
     *
     * @param userId  조회할 사용자 ID
     * @param after   커서에 담긴 조회 시작 시간(Instant). null이면 전체에서 조회
     * @param lastId  커서에 담긴 마지막 ID(UUID). null이면 시간만 기준
     * @param pageable offset/limit + 정렬(생성시간·ID 오름차순)
     */
    List<Notification> findPage(
        UUID userId,
        Instant after,
        UUID lastId,
        Pageable pageable
    );
}