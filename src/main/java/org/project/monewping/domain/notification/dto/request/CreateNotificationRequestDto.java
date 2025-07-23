package org.project.monewping.domain.notification.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;

/**
 * 알림 생성 요청 정보를 담는 DTO입니다.
 *
 * @param userId        알림을 생성할 대상 사용자 ID ({@code null} 불가)
 * @param resourceId    알림이 연관된 리소스의 ID ({@code null} 불가)
 * @param resourceType  리소스 타입 (필수, “Article” 또는 “Comment”만 허용)
 */
public record CreateNotificationRequestDto(
    @NotNull UUID userId,
    @NotNull UUID resourceId,
    @NotBlank @Pattern(regexp = "Article|Comment", message = "resourceType은 Article 또는 Comment만 허용됩니다.") String resourceType
) { }