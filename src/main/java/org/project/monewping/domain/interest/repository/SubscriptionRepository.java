package org.project.monewping.domain.interest.repository;

import org.project.monewping.domain.interest.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    @Query("select s.interest.id from Subscription s where s.user.id = :userId")
    List<UUID> findInterestIdsByUserId(UUID userId);
} 