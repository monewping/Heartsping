package org.project.monewping.domain.notification.service;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.project.monewping.domain.notification.dto.CursorPageResponseNotificationDto;
import org.project.monewping.domain.notification.dto.NotificationDto;

public interface NotificationService {

    List<NotificationDto> create(@NotNull UUID userId, @NotNull UUID resourceId, @NotBlank @Pattern(regexp = "Article|Comment", message = "resourceType은 Article 또는 Comment만 허용됩니다.") String resourceType);

    CursorPageResponseNotificationDto findNotifications(
        @NotNull UUID userId, String cursor, Instant after, @NotNull @Min(1) int limit
    );

    void confirmAll(@NotNull UUID userId);

    void confirmNotification(@NotNull UUID userId, @NotNull UUID notificationId);
}