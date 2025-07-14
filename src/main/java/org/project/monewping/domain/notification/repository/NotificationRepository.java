package org.project.monewping.domain.notification.repository;

import java.time.Instant;
import java.util.UUID;
import org.project.monewping.domain.notification.dto.NotificationDto;
import org.project.monewping.domain.notification.entity.Notification;
import org.project.monewping.global.dto.CursorPageResponse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    CursorPageResponse<NotificationDto> findByUserIdAndAfter(UUID userId, Instant after, int limit);
}
