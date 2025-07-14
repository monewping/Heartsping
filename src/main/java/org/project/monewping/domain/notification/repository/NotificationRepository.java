package org.project.monewping.domain.notification.repository;

import java.util.List;
import java.util.UUID;
import org.project.monewping.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
