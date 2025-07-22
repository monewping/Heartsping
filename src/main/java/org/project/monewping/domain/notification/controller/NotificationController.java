package org.project.monewping.domain.notification.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.monewping.domain.notification.dto.NotificationDto;
import org.project.monewping.domain.notification.dto.request.CreateNotificationRequestDto;
import org.project.monewping.domain.notification.dto.request.GetNotificationsRequestDto;
import org.project.monewping.domain.notification.dto.response.CursorPageResponseNotificationDto;
import org.project.monewping.domain.notification.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
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
        @ModelAttribute @Valid CreateNotificationRequestDto createNotificationRequestDto
    ) {
        UUID userId = createNotificationRequestDto.userId();
        UUID resourceId = createNotificationRequestDto.resourceId();
        String resourceType = createNotificationRequestDto.resourceType();
        log.info("userId = {}, resourceId = {}, resourceType = {}",userId, resourceId, resourceType);

        List<NotificationDto> notificationDtos = notificationService.create(userId, resourceId, resourceType);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(notificationDtos);
    }

    /**
     * 커서 기반으로 사용자 알림 목록을 조회하는 엔드포인트입니다.
     *
     * @param getNotificationsRequestDto 조회 파라미터를 담은 DTO
     * @param userId 요청 헤더 {@code Monew-Request-User-ID}로 전달된 사용자 ID (필수)
     * @return 커서 페이지네이션 결과를 담은 {@link CursorPageResponseNotificationDto}를 HTTP 200 OK로 래핑하여 반환
     */
    @GetMapping
    public ResponseEntity<CursorPageResponseNotificationDto> getNotifications(
        @ModelAttribute @Valid GetNotificationsRequestDto getNotificationsRequestDto,
        @RequestHeader("Monew-Request-User-ID") UUID userId
    ) {
        CursorPageResponseNotificationDto cursorPageResponseNotificationDto =
            notificationService.findNotifications(
                userId,
                getNotificationsRequestDto.cursor(),
                getNotificationsRequestDto.after(),
                getNotificationsRequestDto.limit());

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(cursorPageResponseNotificationDto);
    }

    /**
     * 사용자의 모든 미확인 알림을 확인 처리하는 엔드포인트입니다.
     *
     * @param userId 요청 헤더를 통해 전달된 사용자 ID
     * @return 처리 성공 시 HTTP 200 OK 응답
     */
    @PatchMapping
    public ResponseEntity<Void> confirmAllNotifications(@RequestHeader("Monew-Request-User-ID") UUID userId) {
        notificationService.confirmAll(userId);
        return ResponseEntity.ok().build();
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