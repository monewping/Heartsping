package org.project.monewping.domain.notification.service;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;
import org.project.monewping.domain.interest.entity.Interest;
import org.project.monewping.domain.notification.dto.response.CursorPageResponseNotificationDto;

public interface NotificationService {

    void createNewArticleNotification(Interest interest, int newCount);

    CursorPageResponseNotificationDto findNotifications(
        @NotNull UUID userId,
        String cursor,
        Instant after,
        @NotNull @Min(1) int limit
    );

    void confirmAll(@NotNull UUID userId);

    void confirmNotification(@NotNull UUID userId, @NotNull UUID notificationId);
}