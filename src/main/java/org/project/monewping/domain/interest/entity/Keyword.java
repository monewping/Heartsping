package org.project.monewping.domain.interest.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.Builder;
import org.project.monewping.global.base.BaseEntity;

import java.time.Instant;

/**
 * 관심사와 연관된 키워드를 저장하는 엔티티입니다.
 *
 * <p>특정 관심사에 속하는 키워드 정보를 관리하며,
 * 생성 시점을 자동으로 기록합니다.</p>
 */
@Entity
@Table(name = "keyword")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public class Keyword extends BaseEntity {

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interest_id", nullable = false)
    private Interest interest;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String keyword;

    @Column(nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    /**
     * Keyword 엔티티를 생성합니다.
     *
     * <p>관심사, 키워드, 생성 시점을 받아 엔티티를 초기화합니다.</p>
     * @param interest 관심사
     * @param keyword 키워드 텍스트
     * @param createdAt 생성 시간
     */
    public Keyword(Interest interest, String keyword, Instant createdAt) {
        this.interest = interest;
        this.keyword = keyword;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    /**
     * Keyword 엔티티를 생성합니다.
     *
     * <p>관심사, 키워드를 받아 생성 시점은 현재 시간으로 설정합니다.</p>
     * @param interest 관심사
     * @param keyword 키워드 텍스트
     */
    public Keyword(Interest interest, String keyword) {
        this.interest = interest;
        this.keyword = keyword;
        this.createdAt = Instant.now();
    }
}

