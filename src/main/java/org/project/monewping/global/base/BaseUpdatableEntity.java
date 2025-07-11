package org.project.monewping.global.base;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import lombok.Getter;
import org.springframework.data.annotation.LastModifiedDate;

@Getter
@MappedSuperclass
public abstract class BaseUpdatableEntity extends BaseEntity {

    @Column(name = "updated_at")
    @LastModifiedDate
    private Instant updatedAt;

}
