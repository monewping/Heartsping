package org.project.monewping.domain.notification.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Notification toString() 테스트")
public class NotificationToStringTest {

    @Test
    void testToString() {
        // given
        UUID userId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();
        String content = "알림 내용";
        String resourceType = "Article";

        Notification notification = Notification.builder()
            .userId(userId)
            .resourceId(resourceId)
            .resourceType(resourceType)
            .content(content)
            .confirmed(false)
            .createdAt(Instant.parse("2025-07-18T12:00:00Z"))
            .updatedAt(Instant.parse("2025-07-18T12:00:00Z"))
            .build();

        // when
        String result = notification.toString();

        // then
        assertThat(result)
            .contains("userId=" + userId.toString())
            .contains("resourceId=" + resourceId.toString())
            .contains("resourceType='Article'")
            .contains("content='알림 내용'")
            .contains("confirmed=false");
    }
}
