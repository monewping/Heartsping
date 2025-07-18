package org.project.monewping.domain.notification.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.project.monewping.domain.notification.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    /**
     * 특정 사용자의 읽지 않은 알림 목록 중 첫 페이지를 조회합니다.
     *
     * <p>
     * - {@code userId}로 지정된 사용자의 {@code confirmed=false} 상태인 알림을
     *   {@code createdAt} 및 {@code id} 오름차순으로 정렬하여 조회합니다.
     * - 페이지 크기는 {@code pageable.getPageSize()}를 따르며,
     *   외부에서 {@code limit + 1} 방식으로 요청하여 다음 페이지 존재 여부를 판단할 수 있습니다.
     * </p>
     *
     * @param userId 조회 대상 사용자의 ID
     * @param pageable 정렬 기준(생성일시·ID 오름차순)과 조회할 개수(limit + 1)를 포함한 Pageable 객체
     * @return 정렬된 알림 목록
     */
    @Query("""
      select n
        from Notification n
       where n.userId = :userId
         and n.confirmed = false
       order by n.createdAt asc, n.id asc
    """)
    List<Notification> findPageFirst(
        @Param("userId") UUID userId,
        Pageable pageable
    );

    /**
     * 특정 시점 이후에 생성된 사용자의 읽지 않은 알림 목록을 조회합니다.
     *
     * <p>
     * - {@code userId}로 지정된 사용자의 {@code confirmed=false} 상태인 알림 중,
     *   {@code after} 파라미터로 전달된 시점 이후에 생성된 알림만 조회합니다.
     * - 조회 결과는 {@code createdAt} 및 {@code id} 오름차순으로 정렬되며,
     *   {@code pageable.getPageSize()}만큼 가져옵니다.
     * - 외부에서 {@code limit + 1} 방식으로 요청하여 다음 페이지 존재 여부를 판단할 수 있습니다.
     * </p>
     *
     * @param userId   조회 대상 사용자의 ID
     * @param after    조회 기준이 되는 생성 일시. 해당 시점 이후의 알림을 가져옵니다.
     * @param pageable 정렬 기준(생성일시·ID 오름차순)과 조회할 개수(limit + 1)를 포함한 Pageable 객체
     * @return 정렬된 알림 목록
     */
    @Query("""
      select n
        from Notification n
       where n.userId = :userId
         and n.confirmed = false
         and n.createdAt > :after
       order by n.createdAt asc, n.id asc
    """)
    List<Notification> findPageAfter(
        @Param("userId") UUID userId,
        @Param("after") Instant after,
        Pageable pageable
    );

    long countByUserIdAndConfirmedFalse(UUID userId);

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
}