package org.project.monewping.domain.interest.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.project.monewping.domain.user.entity.User;
import org.project.monewping.global.base.BaseEntity;

/**
 * 관심사 구독 정보를 나타내는 엔티티입니다.
 * <p>사용자(User)와 관심사(Interest) 간의 구독 관계를 표현합니다.</p>
 * <p>한 사용자가 하나의 관심사를 구독할 때마다 한 개의 Subscription이 생성됩니다.</p>
 * <p>user_id와 interest_id의 조합은 유니크합니다.</p>
 */
@Entity
@Table(
        name = "interest_subscriptions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "interest_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Subscription extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interest_id", nullable = false)
    private Interest interest;

    public Subscription(User user, Interest interest) {
        this.user = user;
        this.interest = interest;
    }
}

