package org.project.monewping.domain.notification.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record NotificationDto(
    UUID id,
    UUID userId,
    UUID resourceId,
    String content,
    String resourceType,
    Boolean confirmed,
    Instant createdAt,
    Instant updatedAt
    ) { }
