package org.project.monewping.domain.notification.controller;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.monewping.domain.notification.dto.NotificationDto;
import org.project.monewping.domain.notification.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<List<NotificationDto>> testCreate(
        @RequestParam UUID userId,
        @RequestParam UUID resourceId,
        @RequestParam String resourceType
    ) {
        log.info("userId = {}, resourceId = {}, resourceType = {}", userId, resourceId, resourceType);
        List<NotificationDto> notificationDtos = notificationService.create(userId, resourceId, resourceType);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(notificationDtos);
    }
}