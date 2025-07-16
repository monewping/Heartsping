package org.project.monewping.domain.notification.repository;

import java.time.Instant;
import java.util.List;
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
     * 특정 사용자의 읽지 않은 알림 목록을 커서 기반으로 조회합니다.
     *
     * <p>
     * 이 메서드는 {@code confirmed = false}인 알림만 조회하며,
     * 선택적으로 {@code after} 파라미터를 이용해 기준 시점 이후의 알림을 필터링할 수 있습니다.
     * 정렬 기준은 {@code createdAt} 기준 오름차순이며, 페이징 처리를 위해 {@link Pageable}을 사용합니다.
     * </p>
     *
     * @param userId 알림을 조회할 사용자 ID
     * @param after 커서 기준 시각 (null이면 전체 대상)
     * @param pageable 페이징 정보를 담은 객체
     * @return 조회된 알림 리스트
     */
    @Query("""
      select n
      from Notification n
      where n.userId = :userId
        and n.confirmed = false
        and (cast(:after as timestamp) is null or n.createdAt > :after)
      order by n.createdAt asc
    """)
    List<Notification> findPageSlice(@Param("userId") UUID userId, @Param("after") Instant after, Pageable pageable);

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
}