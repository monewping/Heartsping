package org.project.monewping.domain.notification.controller;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.monewping.domain.notification.dto.CursorPageResponseNotificationDto;
import org.project.monewping.domain.notification.dto.NotificationDto;
import org.project.monewping.domain.notification.service.NotificationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/notifications")
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

    /**
     * 커서 기반으로 사용자 알림 목록을 조회하는 엔드포인트입니다.
     *
     * @param cursor 사용자에게 마지막으로 전달된 커서 토큰 (ISO-8601 문자열, 옵션)
     * @param after  보조 커서(createdAt 필터)로 사용할 Instant (ISO-8601, 옵션)
     * @param limit  한 페이지당 조회할 최대 알림 개수 (필수)
     * @param userId 요청 헤더 {@code Monew-Request-User-ID}로 전달된 사용자 UUID (필수)
     * @return 커서 페이지네이션 결과를 담은 {@link CursorPageResponseNotificationDto}를 HTTP 200 OK로 래핑하여 반환
     */
    @GetMapping
    public ResponseEntity<CursorPageResponseNotificationDto> getNotifications(
        @RequestParam(value = "cursor", required = false) String cursor,
        @RequestParam(value = "after", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant after,
        @RequestParam("limit") int limit,
        @RequestHeader("Monew-Request-User-ID") UUID userId
    ) {
        CursorPageResponseNotificationDto cursorPageResponseNotificationDto =
            notificationService.findNotifications(userId, cursor, after, limit);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(cursorPageResponseNotificationDto);
    }

    /**
     * 특정 알림을 확인된 상태로 처리합니다.
     *
     * <p>
     *     알림 ID와 사용자 ID가 유효한 경우에만 confirmed 필드를 true로 변경하며,
     *     처리가 완료되면 200 OK을 반환합니다.
     * </p>
     *
     * @param notificationId 확인할 알림의 UUID
     * @param userId HTTP 요청 헤더 {@code Monew-Request-User-ID}에 담긴 사용자 UUID
     * @return 처리 성공 시 HTTP 200 OK 응답
     */
    @PatchMapping("/{notificationId}")
    public ResponseEntity<Void> confirmNotification (
        @PathVariable(value = "notificationId") UUID notificationId, @RequestHeader("Monew-Request-User-ID") UUID userId
    ) {
        notificationService.confirmNotification(userId, notificationId);
        return ResponseEntity.ok().build();
    }
}