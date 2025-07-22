package org.project.monewping.domain.notification.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.project.monewping.domain.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID>, NotificationRepositoryCustom {

    /**
     * 사용자의 모든 알림을 읽음 처리합니다.
     *
     * <p>
     * userId 조건에 맞는 알림들을 confirmed=true로 변경합니다.
     * 대량의 알림을 처리하는 경우를 고려해서 벌크 수정 방식을 사용합니다.
     * </p>
     *
     * @param userId 특정 사용자의 id
     * @return 수정된 알림 개수
     */
    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE Notification n
            SET n.confirmed = true
        WHERE n.userId = :userId
            AND n.confirmed = false
        """)
    int confirmAllByUserId(@Param("userId") UUID userId);

    /**
     * userId가 일치하는 notification을 조회합니다.
     *
     * @param notificationId 조회할 알림의 ID
     * @param userId 알림을 조회할 사용자 ID
     * @return Optional.empty()면 존재하지 않는 알림이거나 다른 사용자의 알림
     */
    Optional<Notification> findByIdAndUserId(UUID notificationId, UUID userId);

    /**
     * 확인된(confirmed = true) 알림 중, 1주일이 경과된 알림들을 페이지 단위로 조회합니다.
     *
     * @param updatedAt 기준 시간. 이 시간보다 이전에 updatedAt이 설정된 알림만 조회됩니다.
     * @param pageable 페이징 정보를 포함한 객체입니다. (예: 페이지 크기, 정렬 순서 등)
     * @return 조건에 맞는 {@link Notification} 객체들을 담고 있는 {@link Page}
     */
    Page<Notification> findAllByConfirmedIsTrueAndUpdatedAtBefore(Instant updatedAt, Pageable pageable);

    long countByUserIdAndConfirmedFalse(UUID userId);
}