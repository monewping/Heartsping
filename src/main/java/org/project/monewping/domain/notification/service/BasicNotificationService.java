package org.project.monewping.domain.notification.service;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.monewping.domain.notification.dto.CursorPageResponseNotificationDto;
import org.project.monewping.domain.notification.dto.NotificationDto;
import org.project.monewping.domain.notification.entity.Notification;
import org.project.monewping.domain.notification.exception.UnsupportedResourceTypeException;
import org.project.monewping.domain.notification.mapper.NotificationMapper;
import org.project.monewping.domain.notification.repository.NotificationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Validated
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class BasicNotificationService implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    //private final UserRepository userRepository;
    //private final ArticleRepository articleRepository;
    //private final InterestRepository interestRepository;
    //private final InterestSubscriptionRepository interestSubscriptionRepository;

    private static final int PAGE_OFFSET = 1;

    /**
     * 리소스 별 알림을 생성합니다.
     *
     * <p>resourceTyep 값에 따라 알림 분기 처리를 진행합니다.</p>
     * <ul>
     *   <li><strong>Article</strong> : {@link #createArticleNotifications(UUID, UUID)} 호출</li>
     *   <li><strong>Comment</strong> : {@link #createCommentNotification(UUID, UUID)} 호출</li>
     * </ul>
     *
     * @param userId 알림 대상자의 ID
     * @param resourceId  알림 대상 리소스의 ID
     * @param resourceType 알림의 리소스 타입 ("Article", "Comment")
     * @return 생성/저장된 NotificationDto 리스트
     * @throws UnsupportedResourceTypeException 지원하지 않는 resourceType이 들어올 경우 발생
     */
    @Override
    @Transactional
    public List<NotificationDto> create(UUID userId, UUID resourceId, String resourceType) {
        log.debug("Creating notifications for user {} and resource {}, resourceType {}", userId, resourceId, resourceType);
        List<Notification> notifications;

        switch (resourceType) {
            case "Article": {
                notifications = createArticleNotifications(userId, resourceId);
                break;
            }
            case "Comment": {
                notifications = createCommentNotification(userId, resourceId);
                break;
            }
            default:
                throw new UnsupportedResourceTypeException(resourceType);
        }

        return notifications.stream()
            .map(notificationMapper::toDto)
            .collect(Collectors.toList());
    }

    /**
     * 사용자별 읽지 않은 알림 목록을 페이지네이션을 이용하여 조회합니다.
     *
     * <p>
     * - {@code cursor}가 주어지면 ISO-8601 포맷으로 파싱하여 그 시점 이전 알림을 조회하고,
     *   없다면 {@code after} 값을 기준으로 조회합니다.
     * - 조회 시 {@code limit + 1}개를 가져와서 실제 응답에는 최대 {@code limit}개만 담고
     *   나머지로 {@code hasNext}와 {@code nextCursor}를 계산합니다.
     * - 전체 읽지 않은 알림 개수는 {@code totalElements}에 담아 반환합니다.
     * </p>
     *
     * @param userId 조회할 대상 사용자의 UUID
     * @param cursor 다음 페이지 조회를 위한 커서. null 또는 빈 문자열인 경우 {@code after}를 사용
     * @param after  커서가 없을 때 기준이 될 생성 일시(Instant)
     * @param limit  한 페이지의 최대 조회 알림 개수
     * @return 커서 기반 페이지네이션 응답을 담은 {@link CursorPageResponseNotificationDto}
     * @throws IllegalArgumentException {@code cursor}가 올바른 포맷이 아닐 경우 반환
     */
    @Override
    public CursorPageResponseNotificationDto findNotifications(UUID userId, String cursor, Instant after, int limit) {
        Instant baseAfter = parseBaseAfter(cursor, after);

        Pageable pageable = PageRequest.of(0, limit + PAGE_OFFSET);
        List<Notification> notificationsSlice = notificationRepository.findPageSlice(userId, baseAfter, pageable);

        boolean hasNext = notificationsSlice.size() > limit;
        List<Notification> pageList = hasNext ? notificationsSlice.subList(0, limit) : notificationsSlice;

        List<NotificationDto> content = pageList.stream()
            .map(notificationMapper::toDto)
            .toList();

        Instant nextAfter = null;
        String nextCursor = null;
        if (hasNext) {
            Notification last = pageList.get(pageList.size() - 1);
            nextAfter = last.getCreatedAt();
            nextCursor = nextAfter.toString();
            log.debug("next cursor: {}", nextCursor);
        }

        long totalUnreadNotification = notificationRepository.countByUserIdAndConfirmedFalse(userId);
        log.debug("totalUnreadNotification: {}", totalUnreadNotification);

        return new CursorPageResponseNotificationDto(
            content,
            nextCursor,
            nextAfter,
            limit,
            totalUnreadNotification,
            hasNext
        );
    }

    /**
     * 주어진 사용자 ID에 해당하는 모든 미확인 알림을 확인 처리합니다.
     *
     * <p>
     *     이 메서드는 Notification 엔티티에서 {@code confirmed = false}인 레코드를 찾아
     *     {@code confirmed = true}로 업데이트하며, 처리된 알림의 개수를 로그로 기록합니다.
     *     사용자 정보가 존재하지 않는 경우 (TODO: 예외 처리 추가 예정)
     * </p>
     *
     * @param userId 확인 처리를 수행할 사용자의 ID
     * @throws IllegalArgumentException 사용자 ID가 {@code null}인 경우
     */
    @Override
    @Transactional
    public void confirmAll(UUID userId) {
//        boolean userExists = userRepository.existsById(userId);
//        if (!userExists) {
//            //UserNotFoundException 추가
//        }

        int updatedCount = notificationRepository.confirmAllByUserId(userId);
        log.info("총 {}개의 알림이 확인 처리되었습니다. (userId: {})", updatedCount, userId);
    }

    /**
     * 관심사의 새로운 기사에 대한 알림을 생성합니다. (기능 통합 전 테스트 로직)
     *
     * <p><strong>※ 추후 기능 통합 시 아래 로직으로 대체될 예정입니다.</strong>
     * <ol>
     *   <li>게시글(article)로부터 관심사(interestId)를 조회</li>
     *   <li>해당 관심사에 구독한 사용자 목록(subscriberIds) 조회</li>
     *   <li>가장 최근 알림 이후 생성된 기사 수(count) 집계</li>
     *   <li>구독자 별로 "{interestName}와 관련된 기사가 {count}건 등록되었습니다." 메시지 생성</li>
     * </ol>
     *
     * @param userId 알림 대상자의 ID
     * @param articleId 알림 대상 기사의 ID
     * @return 생성/저장된 Notification 리스트
     */
    private List<Notification> createArticleNotifications (UUID userId, UUID articleId) {
//        Article article = articleRepository.findById(articleId);
//        UUID interestId = article.getInterest().getId();
//
//        String interestName = interestRepository.findById(interestId).getName();
//
//        List<UUID> subscriberIds = interestSubscriptionRepository.findUserIdsByInterestId(interestId);
//
//        long count = articleRepository
//          .countByInterestIdAndCreatedAtAfter(interestId, lastNotifiedAt);
//
//        notifications = subscriberIds.stream()
//          .map(userId -> {
//              String content = String.format(
//                  "%s와 관련된 기사가 %d건 등록되었습니다.",
//                  interestName,
//                  count
//              );
//              return new Notification(userId, content, resourceId,"Article");
//          })
//          .collect(Collectors.toList());
//
//        notificationRepository.saveAll(notifications);

        String interestName = "스포츠";
        int testCount = 2;
        String content = String.format(
            "%s와 관련된 기사가 %d건 등록되었습니다.",
            interestName, testCount
        );

        Notification notification = new Notification(userId, content, articleId, "Article");
        notificationRepository.save(notification);
        return List.of(notification);
    }

    /**
     * 댓글의 좋아요 반응에 대한 알림을 생성합니다.
     *
     * <p><strong>※ 추후 기능 통합 시 로직이 수정될 예정입니다.</strong>
     * <ol>
     *   <li>댓글 작성자 닉네임 조회</li>
     *   <li>"{user}님이 나의 댓글을 좋아합니다." 메시지 생성</li>
     * </ol>
     *
     * @param userId 알림 대상자의 ID
     * @param commentUserId 알림 대상 댓글의 ID
     * @return 생성/저장된 Notification 리스트
     */
    private List<Notification> createCommentNotification (UUID userId, UUID commentUserId) {
        //String user = userRepository.findById(commentUserId).getNickname();
        String testUser = "User";
        String content = testUser + "님이 나의 댓글을 좋아합니다.";

        Notification notification = new Notification(userId, content, commentUserId, "Comment");
        notificationRepository.save(notification);
        return List.of(notification);
    }

    /**
     * cursor 값에 따라 조건 별로 파싱을 진행합니다.
     * <p>
     * - cursor가 null이거나 빈 문자열("")이면 after를 반환합니다.
     * - cursor가 ISO-8601 형식의 날짜·시간 문자열이면 Instant.parse(cursor) 결과를 반환합니다.
     * - cursor가 잘못된 포맷이면 IllegalArgumentException을 던집니다.
     *
     * @param cursor ISO-8601 형식의 커서 문자열 혹은 null
     * @param after  cursor가 없을 때 사용될 기준 시점 Instant
     * @return cursor를 파싱한 Instant, 또는 cursor가 없으면 after
     * @throws IllegalArgumentException cursor가 올바른 포맷이 아닐 때
     */
    private Instant parseBaseAfter(String cursor, Instant after) {
        if (cursor != null && !cursor.isEmpty()) {
            try {
                return Instant.parse(cursor);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid cursor: " + cursor, e);
            }
        }
        return after;
    }
}