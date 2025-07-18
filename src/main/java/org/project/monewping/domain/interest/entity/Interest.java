package org.project.monewping.domain.interest.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.project.monewping.global.base.BaseUpdatableEntity;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

/**
 * 관심사 정보를 저장하는 엔티티입니다.
 *
 * <p>관심사 이름, 구독자 수, 키워드 목록을 관리하며
 * 키워드와의 연관관계를 설정합니다.</p>
 */
@Entity
@Table(name = "interests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public class Interest extends BaseUpdatableEntity {

    @Column(length = 100, nullable = false, unique = true)
    private String name;

    @Builder.Default
    @Column(nullable = false, name = "subscriber_count")
    private Long subscriberCount = 0L;

    @OneToMany(mappedBy = "interest", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Keyword> keywords = new ArrayList<>();

    public Interest(String name, Long subscriberCount, List<Keyword> keywords) {
        this.name = name;
        this.subscriberCount = subscriberCount;
        this.keywords = keywords != null ? keywords : new ArrayList<>();
    }

    /**
     * 구독자 수를 1 증가시킵니다.
     *
     * <p>관심사에 대한 구독자 수를 증가시킵니다.</p>
     */
    public void increaseSubscriber() {
        this.subscriberCount++;
    }

    /**
     * 구독자 수를 1 감소시킵니다.
     *
     * <p>구독자 수가 0보다 클 때만 감소합니다.</p>
     */
    public void decreaseSubscriber() {
        if (this.subscriberCount > 0) {
            this.subscriberCount--;
        }
    }

    /**
     * 키워드를 추가합니다.
     *
     * <p>관심사와 키워드의 연관관계를 설정합니다.</p>
     * @param keyword 추가할 키워드
     */
    public void addKeyword(Keyword keyword) {
        this.keywords.add(keyword);
        keyword.setInterest(this);
    }



    /**
     * 키워드 목록을 업데이트합니다.
     *
     * <p>기존 키워드를 모두 제거하고 새로운 키워드 목록으로 교체합니다.</p>
     * @param newKeywords 새로운 키워드 이름 목록
     */
    public void updateKeywords(List<String> newKeywords) {
        // 기존 키워드 모두 제거
        this.keywords.clear();
        
        // 새로운 키워드 추가
        if (newKeywords != null) {
            newKeywords.stream()
                    .filter(keyword -> keyword != null && !keyword.trim().isEmpty())
                    .forEach(keywordName -> {
                        Keyword keyword = new Keyword(this, keywordName.trim());
                        this.keywords.add(keyword);
                    });
        }
    }
}
