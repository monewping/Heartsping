package org.project.monewping.domain.article.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.project.monewping.global.base.BaseEntity;

@Entity
@Table(name = "interests")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Interest extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "subscriber_count", nullable = false)
    private long subscriberCount;


    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}
