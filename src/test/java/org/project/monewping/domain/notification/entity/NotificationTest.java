package org.project.monewping.domain.notification.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Notification 엔티티 테스트")
class NotificationTest {

    private UUID userId;
    private UUID resourceId;
    private String content;
    private String resourceType;
    private Notification notification;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        resourceId = UUID.randomUUID();
        content = "테스트 알림 내용";
        resourceType = "Article";
        
        notification = new Notification(userId, content, resourceId, resourceType);
    }

    @Test
    @DisplayName("Notification 생성자 테스트")
    void testConstructor() {
        // given
        UUID newUserId = UUID.randomUUID();
        UUID newResourceId = UUID.randomUUID();
        String newContent = "새로운 알림 내용";
        String newResourceType = "Comment";

        // when
        Notification newNotification = new Notification(newUserId, newContent, newResourceId, newResourceType);

        // then
        assertThat(newNotification.getUserId()).isEqualTo(newUserId);
        assertThat(newNotification.getContent()).isEqualTo(newContent);
        assertThat(newNotification.getResourceId()).isEqualTo(newResourceId);
        assertThat(newNotification.getResourceType()).isEqualTo(newResourceType);
        assertThat(newNotification.getConfirmed()).isFalse();
    }

    @Test
    @DisplayName("기본 생성자 테스트")
    void testDefaultConstructor() {
        // given & when
        Notification defaultNotification = new Notification();

        // then
        assertThat(defaultNotification.getUserId()).isNull();
        assertThat(defaultNotification.getContent()).isNull();
        assertThat(defaultNotification.getResourceId()).isNull();
        assertThat(defaultNotification.getResourceType()).isNull();
        assertThat(defaultNotification.getConfirmed()).isNull();
    }

    @Test
    @DisplayName("ofForTest 정적 메서드 테스트")
    void testOfForTest() {
        // given
        UUID testUserId = UUID.randomUUID();
        UUID testResourceId = UUID.randomUUID();
        String testContent = "테스트용 알림";
        String testResourceType = "Interest";
        Instant testCreatedAt = Instant.parse("2025-01-01T12:00:00Z");

        // when
        Notification testNotification = Notification.ofForTest(
            testUserId, testContent, testResourceId, testResourceType, testCreatedAt);

        // then
        assertThat(testNotification.getUserId()).isEqualTo(testUserId);
        assertThat(testNotification.getContent()).isEqualTo(testContent);
        assertThat(testNotification.getResourceId()).isEqualTo(testResourceId);
        assertThat(testNotification.getResourceType()).isEqualTo(testResourceType);
        assertThat(testNotification.getConfirmed()).isFalse();
        assertThat(testNotification.getCreatedAt()).isEqualTo(testCreatedAt);
    }

    @Test
    @DisplayName("setCreatedAtForTest 메서드 테스트")
    void testSetCreatedAtForTest() {
        // given
        Instant newCreatedAt = Instant.parse("2025-12-31T23:59:59Z");

        // when
        notification.setCreatedAtForTest(newCreatedAt);

        // then
        assertThat(notification.getCreatedAt()).isEqualTo(newCreatedAt);
    }

    @Test
    @DisplayName("toString 메서드 테스트")
    void testToString() {
        // when
        String result = notification.toString();

        // then
        assertThat(result).contains("Notification {");
        assertThat(result).contains("userId=" + userId);
        assertThat(result).contains("content='" + content + "'");
        assertThat(result).contains("resourceId=" + resourceId);
        assertThat(result).contains("resourceType='" + resourceType + "'");
        assertThat(result).contains("confirmed=false");
    }

    @Test
    @DisplayName("다양한 리소스 타입 테스트")
    void testDifferentResourceTypes() {
        // given
        String[] resourceTypes = {"Article", "Comment", "Interest", "User"};

        for (String type : resourceTypes) {
            // when
            Notification typeNotification = new Notification(userId, content, resourceId, type);

            // then
            assertThat(typeNotification.getResourceType()).isEqualTo(type);
        }
    }

    @Test
    @DisplayName("긴 알림 내용 테스트")
    void testLongContent() {
        // given
        String longContent = "이것은 매우 긴 알림 내용입니다. " +
            "사용자에게 중요한 정보를 전달하기 위해 작성된 알림으로, " +
            "여러 줄에 걸쳐 상세한 내용을 포함할 수 있습니다.";

        // when
        Notification longNotification = new Notification(userId, longContent, resourceId, resourceType);

        // then
        assertThat(longNotification.getContent()).isEqualTo(longContent);
    }

    @Test
    @DisplayName("특수 문자 포함 알림 내용 테스트")
    void testSpecialCharactersInContent() {
        // given
        String specialContent = "특수문자 테스트: !@#$%^&*()_+-=[]{}|;':\",./<>?";

        // when
        Notification specialNotification = new Notification(userId, specialContent, resourceId, resourceType);

        // then
        assertThat(specialNotification.getContent()).isEqualTo(specialContent);
    }

    @Test
    @DisplayName("빈 문자열 알림 내용 테스트")
    void testEmptyContent() {
        // given
        String emptyContent = "";

        // when
        Notification emptyNotification = new Notification(userId, emptyContent, resourceId, resourceType);

        // then
        assertThat(emptyNotification.getContent()).isEqualTo(emptyContent);
    }

    @Test
    @DisplayName("null 값 처리 테스트")
    void testNullValues() {
        // given & when
        Notification nullNotification = new Notification(null, null, null, null);

        // then
        assertThat(nullNotification.getUserId()).isNull();
        assertThat(nullNotification.getContent()).isNull();
        assertThat(nullNotification.getResourceId()).isNull();
        assertThat(nullNotification.getResourceType()).isNull();
        assertThat(nullNotification.getConfirmed()).isFalse(); // 생성자에서 false로 설정됨
    }

    @Test
    @DisplayName("UUID 값 테스트")
    void testUuidValues() {
        // given
        UUID testUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        UUID testResourceId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");

        // when
        Notification uuidNotification = new Notification(testUserId, content, testResourceId, resourceType);

        // then
        assertThat(uuidNotification.getUserId()).isEqualTo(testUserId);
        assertThat(uuidNotification.getResourceId()).isEqualTo(testResourceId);
    }

    @Test
    @DisplayName("여러 알림 생성 테스트")
    void testMultipleNotifications() {
        // given
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();
        UUID resource1 = UUID.randomUUID();
        UUID resource2 = UUID.randomUUID();

        // when
        Notification notification1 = new Notification(user1, "첫 번째 알림", resource1, "Article");
        Notification notification2 = new Notification(user2, "두 번째 알림", resource2, "Comment");

        // then
        assertThat(notification1.getUserId()).isEqualTo(user1);
        assertThat(notification1.getContent()).isEqualTo("첫 번째 알림");
        assertThat(notification1.getResourceId()).isEqualTo(resource1);
        assertThat(notification1.getResourceType()).isEqualTo("Article");

        assertThat(notification2.getUserId()).isEqualTo(user2);
        assertThat(notification2.getContent()).isEqualTo("두 번째 알림");
        assertThat(notification2.getResourceId()).isEqualTo(resource2);
        assertThat(notification2.getResourceType()).isEqualTo("Comment");
    }

    @Test
    @DisplayName("확인 상태 기본값 테스트")
    void testDefaultConfirmedStatus() {
        // given & when
        Notification newNotification = new Notification(userId, content, resourceId, resourceType);

        // then
        assertThat(newNotification.getConfirmed()).isFalse();
    }

    @Test
    @DisplayName("전체 필드 통합 테스트")
    void testAllFieldsIntegration() {
        // given
        UUID testUserId = UUID.randomUUID();
        UUID testResourceId = UUID.randomUUID();
        String testContent = "통합 테스트 알림 내용";
        String testResourceType = "Article";
        Instant testCreatedAt = Instant.parse("2025-06-15T14:30:00Z");

        // when
        Notification testNotification = Notification.ofForTest(
            testUserId, testContent, testResourceId, testResourceType, testCreatedAt);

        // then
        assertThat(testNotification.getUserId()).isEqualTo(testUserId);
        assertThat(testNotification.getContent()).isEqualTo(testContent);
        assertThat(testNotification.getResourceId()).isEqualTo(testResourceId);
        assertThat(testNotification.getResourceType()).isEqualTo(testResourceType);
        assertThat(testNotification.getConfirmed()).isFalse();
        assertThat(testNotification.getCreatedAt()).isEqualTo(testCreatedAt);
    }
} 