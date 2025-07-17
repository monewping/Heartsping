package org.project.monewping.domain.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.project.monewping.global.base.BaseUpdatableEntity;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseUpdatableEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "resource_id", nullable = false)
    private UUID resourceId;

    @Column(name = "resource_type", nullable = false)
    private String resourceType;

    @Column(name = "confirmed", nullable = false)
    private Boolean confirmed;

    public Notification(UUID userId, String content, UUID resourceId, String resourceType) {
        this.userId = userId;
        this.content = content;
        this.resourceId = resourceId;
        this.resourceType = resourceType;
        this.confirmed = false;
    }

    /*
        알림 목록 조회 테스트 용 생성자
     */
    public static Notification ofForTest(UUID userId, String content, UUID resourceId, String resourceType, Instant createdAt) {
        Notification notification = new Notification(userId, content, resourceId, resourceType);
        notification.setCreatedAtForTest(createdAt);
        return notification;
    }

    protected void setCreatedAtForTest(Instant createdAt) {
        super.setCreatedAt(createdAt);
    }

    @Override
    public String toString() {
        return "Notification {" +
            "userId=" + userId +
            ", content='" + content + '\'' +
            ", resourceId=" + resourceId +
            ", resourceType='" + resourceType + '\'' +
            ", confirmed=" + confirmed +
            '}';
    }
}
