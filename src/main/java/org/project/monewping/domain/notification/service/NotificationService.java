package org.project.monewping.domain.notification.service;

import java.util.List;
import java.util.UUID;
import org.project.monewping.domain.notification.dto.NotificationDto;

public interface NotificationService {

    List<NotificationDto> create(UUID userId, UUID resourceId, String resourceType);
}
