package org.project.monewping.domain.interest.repository;

import org.project.monewping.domain.interest.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    @Query("select s.interest.id from Subscription s where s.user.id = :userId")
    List<UUID> findInterestIdsByUserId(UUID userId);

    /**
     * 사용자와 관심사로 구독을 찾습니다.
     *
     * @param userId 사용자 ID
     * @param interestId 관심사 ID
     * @return 구독 정보 (Optional)
     */
    @Query("select s from Subscription s where s.user.id = :userId and s.interest.id = :interestId")
    Optional<Subscription> findByUserIdAndInterestId(UUID userId, UUID interestId);

    /**
     * 사용자와 관심사로 구독이 존재하는지 확인합니다.
     *
     * @param userId 사용자 ID
     * @param interestId 관심사 ID
     * @return 구독 존재 여부
     */
    @Query("select count(s) > 0 from Subscription s where s.user.id = :userId and s.interest.id = :interestId")
    boolean existsByUserIdAndInterestId(UUID userId, UUID interestId);
} 