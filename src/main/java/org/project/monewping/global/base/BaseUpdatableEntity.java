package org.project.monewping.global.base;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
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
  private LocalDateTime updatedAt;
}
