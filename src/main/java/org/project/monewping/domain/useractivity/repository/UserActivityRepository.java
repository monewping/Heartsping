package org.project.monewping.domain.useractivity.repository;

import org.project.monewping.domain.useractivity.document.UserActivityDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * 사용자 활동 내역 MongoDB Repository
 * 
 * <p>
 * 사용자 활동 내역 Document의 CRUD 작업을 담당합니다.
 * </p>
 */
@Repository
public interface UserActivityRepository extends MongoRepository<UserActivityDocument, UUID> {

    /**
     * 사용자 ID로 활동 내역을 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 사용자 활동 내역 (없으면 Optional.empty())
     */
    Optional<UserActivityDocument> findByUserId(UUID userId);

    /**
     * 사용자 ID로 활동 내역이 존재하는지 확인합니다.
     * 
     * @param userId 사용자 ID
     * @return 존재 여부
     */
    boolean existsByUserId(UUID userId);

    /**
     * 사용자 ID로 활동 내역을 삭제합니다.
     * 
     * @param userId 사용자 ID
     */
    void deleteByUserId(UUID userId);

    /**
     * 특정 시간 이후에 업데이트된 활동 내역을 조회합니다.
     * 
     * @param updatedAt 기준 시간
     * @return 업데이트된 활동 내역 목록
     */
    @Query("{ 'updatedAt': { $gte: ?0 } }")
    java.util.List<UserActivityDocument> findByUpdatedAtAfter(java.time.Instant updatedAt);
}
