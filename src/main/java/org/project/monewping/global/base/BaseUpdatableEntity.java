package org.project.monewping.global.base;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.LastModifiedDate;

@Getter
@MappedSuperclass
@SuperBuilder
@NoArgsConstructor
public abstract class BaseUpdatableEntity extends BaseEntity {

    @Column(name = "updated_at")
    @LastModifiedDate
    private Instant updatedAt;

    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
