package org.project.monewping.domain.notification.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Notification 엔티티 통합 테스트")
class NotificationEntityTest {

    @Test
    @DisplayName("Notification 기본 생성자 테스트")
    void testDefaultConstructor() {
        // given & when
        Notification notification = new Notification();
        
        // then
        assertThat(notification).isNotNull();
        assertThat(notification.getId()).isNull();
        assertThat(notification.getContent()).isNull();
        assertThat(notification.getResourceType()).isNull();
        assertThat(notification.getResourceId()).isNull();
        assertThat(notification.getUserId()).isNull();
        assertThat(notification.getConfirmed()).isNull();
    }

    @Test
    @DisplayName("Notification 생성자 테스트")
    void testConstructor() {
        // given
        UUID userId = UUID.randomUUID();
        String content = "테스트 알림 내용";
        String resourceType = "ARTICLE";
        UUID resourceId = UUID.randomUUID();
        
        // when
        Notification notification = new Notification(userId, content, resourceId, resourceType);
        
        // then
        assertThat(notification.getUserId()).isEqualTo(userId);
        assertThat(notification.getContent()).isEqualTo(content);
        assertThat(notification.getResourceId()).isEqualTo(resourceId);
        assertThat(notification.getResourceType()).isEqualTo(resourceType);
        assertThat(notification.getConfirmed()).isFalse();
    }

    @Test
    @DisplayName("Notification 엔티티 생성 및 저장 테스트")
    void testEntityCreationAndPersistence() {
        // given
        UUID userId = UUID.randomUUID();
        String content = "저장 테스트 알림 내용";
        String resourceType = "USER";
        UUID resourceId = UUID.randomUUID();
        
        // when
        Notification notification = new Notification(userId, content, resourceId, resourceType);
        
        // then
        assertThat(notification).isNotNull();
        assertThat(notification.getUserId()).isEqualTo(userId);
        assertThat(notification.getContent()).isEqualTo(content);
        assertThat(notification.getResourceType()).isEqualTo(resourceType);
        assertThat(notification.getResourceId()).isEqualTo(resourceId);
        assertThat(notification.getConfirmed()).isFalse();
    }

    @Test
    @DisplayName("Notification 전체 필드 통합 테스트")
    void testAllFieldsIntegration() {
        // given
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        String content1 = "알림 내용 1";
        String content2 = "알림 내용 2";
        String resourceType1 = "ARTICLE";
        String resourceType2 = "INTEREST";
        UUID resourceId1 = UUID.randomUUID();
        UUID resourceId2 = UUID.randomUUID();
        
        // when
        Notification notification1 = new Notification(userId1, content1, resourceId1, resourceType1);
        Notification notification2 = new Notification(userId2, content2, resourceId2, resourceType2);
        
        // then
        assertThat(notification1).isNotEqualTo(notification2);
        assertThat(notification1.getContent()).isNotEqualTo(notification2.getContent());
        assertThat(notification1.getResourceType()).isNotEqualTo(notification2.getResourceType());
        assertThat(notification1.getResourceId()).isNotEqualTo(notification2.getResourceId());
        assertThat(notification1.getUserId()).isNotEqualTo(notification2.getUserId());
    }

    @Test
    @DisplayName("확인 상태 기본값 테스트")
    void testDefaultCheckedStatus() {
        // given & when
        Notification notification = new Notification(UUID.randomUUID(), "테스트 내용", UUID.randomUUID(), "ARTICLE");
        
        // then
        assertThat(notification.getConfirmed()).isFalse();
    }

    @Test
    @DisplayName("다양한 리소스 타입 테스트")
    void testVariousResourceTypes() {
        // given
        String[] resourceTypes = {"ARTICLE", "INTEREST", "USER", "COMMENT", "NOTIFICATION", "SYSTEM"};
        
        // when & then
        for (String resourceType : resourceTypes) {
            Notification notification = new Notification(UUID.randomUUID(), "테스트 알림", UUID.randomUUID(), resourceType);
            
            assertThat(notification.getResourceType()).isEqualTo(resourceType);
        }
    }

    @Test
    @DisplayName("긴 알림 내용 테스트")
    void testLongContent() {
        // given
        String longContent = "이것은 매우 긴 알림 내용입니다. ".repeat(100);
        
        // when
        Notification notification = new Notification(UUID.randomUUID(), longContent, UUID.randomUUID(), "ARTICLE");
        
        // then
        assertThat(notification.getContent()).isEqualTo(longContent);
    }

    @Test
    @DisplayName("특수 문자 포함 알림 내용 테스트")
    void testSpecialCharactersInContent() {
        // given
        String specialContent = "특수문자: !@#$%^&*()_+-=[]{}|;':\",./<>?";
        
        // when
        Notification notification = new Notification(UUID.randomUUID(), specialContent, UUID.randomUUID(), "ARTICLE");
        
        // then
        assertThat(notification.getContent()).isEqualTo(specialContent);
    }

    @Test
    @DisplayName("빈 문자열 알림 내용 테스트")
    void testEmptyContent() {
        // given
        String emptyContent = "";
        
        // when
        Notification notification = new Notification(UUID.randomUUID(), emptyContent, UUID.randomUUID(), "ARTICLE");
        
        // then
        assertThat(notification.getContent()).isEqualTo(emptyContent);
    }

    @Test
    @DisplayName("null 값 처리 테스트")
    void testNullValues() {
        // given & when
        Notification notification = new Notification();
        
        // then
        assertThat(notification.getContent()).isNull();
        assertThat(notification.getResourceType()).isNull();
        assertThat(notification.getResourceId()).isNull();
        assertThat(notification.getUserId()).isNull();
    }

    @Test
    @DisplayName("여러 알림 생성 테스트")
    void testMultipleNotifications() {
        // given & when
        Notification[] notifications = new Notification[10];
        for (int i = 0; i < 10; i++) {
            notifications[i] = new Notification(UUID.randomUUID(), "알림 " + (i + 1), UUID.randomUUID(), "ARTICLE");
        }
        
        // then
        for (int i = 0; i < 10; i++) {
            assertThat(notifications[i].getContent()).isEqualTo("알림 " + (i + 1));
        }
    }

    @Test
    @DisplayName("toString 메서드 테스트")
    void testToString() {
        // given
        UUID userId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();
        Notification notification = new Notification(userId, "테스트 알림", resourceId, "ARTICLE");
        
        // when
        String result = notification.toString();
        
        // then
        assertThat(result).isNotNull();
        assertThat(result).contains("테스트 알림");
        assertThat(result).contains("ARTICLE");
        assertThat(result).contains(userId.toString());
        assertThat(result).contains(resourceId.toString());
    }

    @Test
    @DisplayName("UUID 값 테스트")
    void testUuidValue() {
        // given
        UUID userId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();
        
        // when
        Notification notification = new Notification(userId, "UUID 테스트", resourceId, "ARTICLE");
        
        // then
        assertThat(notification.getUserId()).isEqualTo(userId);
        assertThat(notification.getResourceId()).isEqualTo(resourceId);
    }

    @Test
    @DisplayName("ofForTest 정적 메서드 테스트")
    void testOfForTestStaticMethod() {
        // given
        UUID userId = UUID.randomUUID();
        String content = "테스트 알림";
        String resourceType = "ARTICLE";
        UUID resourceId = UUID.randomUUID();
        Instant createdAt = Instant.now();
        
        // when
        Notification notification = Notification.ofForTest(userId, content, resourceId, resourceType, createdAt);
        
        // then
        assertThat(notification.getUserId()).isEqualTo(userId);
        assertThat(notification.getContent()).isEqualTo(content);
        assertThat(notification.getResourceType()).isEqualTo(resourceType);
        assertThat(notification.getResourceId()).isEqualTo(resourceId);
        assertThat(notification.getConfirmed()).isFalse();
    }

    @Test
    @DisplayName("setCreatedAtForTest 메서드 테스트")
    void testSetCreatedAtForTest() {
        // given
        Notification notification = new Notification(UUID.randomUUID(), "테스트 알림", UUID.randomUUID(), "ARTICLE");
        Instant testTime = Instant.parse("2025-01-01T12:00:00Z");
        
        // when
        notification.setCreatedAtForTest(testTime);
        
        // then
        assertThat(notification.getCreatedAt()).isEqualTo(testTime);
    }

    @Test
    @DisplayName("Notification 다양한 시간대 테스트")
    void testVariousTimeZones() {
        // given
        UUID userId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();
        Instant pastTime = Instant.parse("2024-01-01T12:00:00Z");
        Instant currentTime = Instant.now();
        Instant futureTime = Instant.parse("2026-01-01T12:00:00Z");
        
        // when
        Notification pastNotification = Notification.ofForTest(userId, "과거 알림", resourceId, "ARTICLE", pastTime);
        Notification currentNotification = Notification.ofForTest(userId, "현재 알림", resourceId, "ARTICLE", currentTime);
        Notification futureNotification = Notification.ofForTest(userId, "미래 알림", resourceId, "ARTICLE", futureTime);
        
        // then
        assertThat(pastNotification.getCreatedAt()).isBefore(currentTime);
        assertThat(futureNotification.getCreatedAt()).isAfter(currentTime);
        assertThat(currentNotification.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Notification 한글 내용 테스트")
    void testKoreanContent() {
        // given
        String koreanContent = "안녕하세요! 이것은 한글 알림 내용입니다. 1234567890";
        
        // when
        Notification notification = new Notification(UUID.randomUUID(), koreanContent, UUID.randomUUID(), "ARTICLE");
        
        // then
        assertThat(notification.getContent()).isEqualTo(koreanContent);
    }

    @Test
    @DisplayName("Notification 영어 내용 테스트")
    void testEnglishContent() {
        // given
        String englishContent = "Hello! This is an English notification content. 1234567890";
        
        // when
        Notification notification = new Notification(UUID.randomUUID(), englishContent, UUID.randomUUID(), "ARTICLE");
        
        // then
        assertThat(notification.getContent()).isEqualTo(englishContent);
    }

    @Test
    @DisplayName("Notification 숫자와 기호 테스트")
    void testNumbersAndSymbols() {
        // given
        String numberContent = "알림 번호: #12345, 우선순위: 1, 상태: ACTIVE";
        
        // when
        Notification notification = new Notification(UUID.randomUUID(), numberContent, UUID.randomUUID(), "SYSTEM");
        
        // then
        assertThat(notification.getContent()).isEqualTo(numberContent);
        assertThat(notification.getResourceType()).isEqualTo("SYSTEM");
    }

    @Test
    @DisplayName("Notification 여러 사용자 테스트")
    void testMultipleUsers() {
        // given & when
        UUID[] userIds = new UUID[5];
        Notification[] notifications = new Notification[5];
        
        for (int i = 0; i < 5; i++) {
            userIds[i] = UUID.randomUUID();
            notifications[i] = new Notification(userIds[i], "사용자 " + (i + 1) + " 알림", UUID.randomUUID(), "USER");
        }
        
        // then
        for (int i = 0; i < 5; i++) {
            assertThat(notifications[i].getUserId()).isEqualTo(userIds[i]);
            assertThat(notifications[i].getContent()).isEqualTo("사용자 " + (i + 1) + " 알림");
            assertThat(notifications[i].getResourceType()).isEqualTo("USER");
        }
    }

    @Test
    @DisplayName("Notification 동일한 리소스 ID 테스트")
    void testSameResourceId() {
        // given
        UUID resourceId = UUID.randomUUID();
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();
        
        // when
        Notification notification1 = new Notification(user1, "사용자1 알림", resourceId, "ARTICLE");
        Notification notification2 = new Notification(user2, "사용자2 알림", resourceId, "ARTICLE");
        
        // then
        assertThat(notification1.getResourceId()).isEqualTo(resourceId);
        assertThat(notification2.getResourceId()).isEqualTo(resourceId);
        assertThat(notification1.getUserId()).isNotEqualTo(notification2.getUserId());
    }
} 