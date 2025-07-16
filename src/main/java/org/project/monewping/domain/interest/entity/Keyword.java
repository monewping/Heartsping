package org.project.monewping.domain.interest.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.Builder;
import org.project.monewping.global.base.BaseEntity;

/**
 * 관심사와 연관된 키워드를 저장하는 엔티티입니다.
 *
 * <p>특정 관심사에 속하는 키워드 정보를 관리하며,
 * 생성 시점은 BaseEntity에서 자동으로 기록됩니다.</p>
 */
@Entity
@Table(name = "keywords")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public class Keyword extends BaseEntity {

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interest_id", nullable = false)
    private Interest interest;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String name;

    public Keyword(Interest interest, String name) {
        this.interest = interest;
        this.name = name;
    }
}

