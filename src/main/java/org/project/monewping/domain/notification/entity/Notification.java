package org.project.monewping.domain.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.project.monewping.global.base.BaseUpdatableEntity;

@Entity
@Table(name = "notifications")
@SuperBuilder
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
    private boolean confirmed;

    @Column(name = "active", nullable = false)
    private boolean active;

    public Notification(UUID userId, String content, UUID resourceId, String resourceType) {
        this.userId = userId;
        this.content = content;
        this.resourceId = resourceId;
        this.resourceType = resourceType;
        this.confirmed = false;
    }

    public void confirm() {
        this.confirmed = true;
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
