package org.project.monewping.domain.useractivity.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.monewping.domain.useractivity.dto.UserActivityDto;
import org.project.monewping.domain.useractivity.service.UserActivityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * 사용자 활동 내역 관리 Controller
 * 
 * <p>
 * 사용자 활동 내역 조회 API를 제공합니다.
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/api/user-activities")
@RequiredArgsConstructor
public class UserActivityController {

    private final UserActivityService userActivityService;

    /**
     * 사용자 활동 내역을 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 사용자 활동 내역 DTO
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserActivityDto> getUserActivity(@PathVariable UUID userId) {
        log.info("사용자 활동 내역 조회 요청. userId: {}", userId);

        UserActivityDto userActivity = userActivityService.getUserActivity(userId);

        log.info("사용자 활동 내역 조회 완료. userId: {}", userId);
        return ResponseEntity.ok(userActivity);
    }
}
