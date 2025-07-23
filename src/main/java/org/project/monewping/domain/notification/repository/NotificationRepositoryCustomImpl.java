package org.project.monewping.domain.notification.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.project.monewping.domain.notification.entity.Notification;
import org.project.monewping.domain.notification.entity.QNotification;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class NotificationRepositoryCustomImpl implements NotificationRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QNotification notification = QNotification.notification;

    /**
     * 특정 사용자의 미확인 알림을 커서(cursor) 기반으로 페이지 조회합니다.
     *
     * <p>기본적으로 userId에 해당하는 알림 중 confirmed=false인 것만 조회하며,
     * 이후 조회 기준이 되는 {@code after} 시점과 {@code lastId}를 활용해
     * 커서 기반 페이징을 지원합니다.</p>
     *
     * @param userId   알림을 조회할 대상 사용자 ID
     * @param after    커서 기준 시점(Instant). {@code null}인 경우 전체 데이터를 대상으로 조회
     * @param lastId   같은 시각에 여러 알림이 존재할 때, 마지막으로 본 알림 ID. {@code null} 가능
     * @param pageable 페이지 정보(Offset, Limit 등)
     * @return 조회된 {@code Notification} 객체들의 리스트
     */
    @Override
    public List<Notification> findPage(
        UUID userId,
        Instant after,
        UUID lastId,
        Pageable pageable
    ) {
        BooleanExpression predicate = notification.userId.eq(userId)
            .and(notification.confirmed.isFalse());

        // 커서(after, lastId) 조합에 따른 분기
        if (after != null) {
            BooleanExpression timeCond = notification.createdAt.gt(after);

            if (lastId != null) {
                timeCond = timeCond.or(
                    notification.createdAt.eq(after)
                        .and(notification.id.gt(lastId))
                );
            }

            predicate = predicate.and(timeCond);
        }

        return queryFactory
            .selectFrom(notification)
            .where(predicate)
            .orderBy(notification.createdAt.asc(), notification.id.asc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();
    }
}