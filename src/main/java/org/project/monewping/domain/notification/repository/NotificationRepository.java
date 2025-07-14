package org.project.monewping.domain.notification.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.project.monewping.domain.notification.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    @Query("""
      select n
      from Notification n
      where n.userId = :userId
        and n.confirmed = false
        and (cast(:after as timestamp) is null or n.createdAt < :after)
      order by n.createdAt desc
    """)
    List<Notification> findPageSlice(@Param("userId") UUID userId, @Param("after") Instant after, Pageable pageable);

    long countByUserIdAndConfirmedFalse(UUID userId);
}