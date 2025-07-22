package org.project.monewping.domain.notification.service;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.monewping.domain.notification.dto.response.CursorPageResponseNotificationDto;
import org.project.monewping.domain.notification.dto.NotificationDto;
import org.project.monewping.domain.notification.entity.Notification;
import org.project.monewping.domain.notification.exception.InvalidCursorFormatException;
import org.project.monewping.domain.notification.exception.NotificationNotFoundException;
import org.project.monewping.domain.notification.exception.UnsupportedResourceTypeException;
import org.project.monewping.domain.notification.mapper.NotificationMapper;
import org.project.monewping.domain.notification.repository.NotificationRepository;
import org.project.monewping.domain.user.exception.UserNotFoundException;
import org.project.monewping.domain.user.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private final UserRepository userRepository;
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
        log.debug("알림 생성 - userId: {}, resourceId: {}, resourceType: {}", userId, resourceId, resourceType);
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
     * 특정 사용자의 읽지 않은 알림 목록을 페이지네이션을 이용하여 조회합니다.
     *
     * <p>
     *   {@code cursor} 문자열을 파싱해 기준 시점({@code after})과 마지막 알림 ID({@code parsedId})를
     *   추출하며, 이를 이용해 알림을 오름차순으로 조회합니다. 조회된 결과에서 {@code limit} 개보다 한 건 더
     *   가져와서 {@code hasNext} 플래그를 계산하고, 실제 반환할 목록을 {@code limit} 크기로 잘라냅니다.
     * </p>
     *
     * @param userId 조회할 대상 사용자의 UUID
     * @param cursor 이전 페이지의 커서 문자열 (형식: {@code "createdAt|id"}). {@code null} 또는 빈 문자열인 경우 처음부터 조회
     * @param after  커서가 없을 때 기준이 될 생성 일시(Instant), {@code cursor}가 없을 때 기본값으로 사용
     * @param limit  한 페이지의 최대 조회 알림 개수
     * @return 커서 기반 페이지네이션 응답을 담은 {@link CursorPageResponseNotificationDto}
     * @throws InvalidCursorFormatException {@code cursor}가 올바른 포맷이 아닐 경우 반환
     */
    @Override
    public CursorPageResponseNotificationDto findNotifications(UUID userId, String cursor, Instant after, int limit) {
        Instant parsedTime = after;
        UUID parsedId = null;

        if (cursor != null && !cursor.isBlank()) {
            try {
                String[] parts = cursor.split("\\|", 2);
                if (parts.length < 1) {
                    throw new IllegalArgumentException("cursor 파싱 실패");
                }
                parsedTime = Instant.parse(parts[0]);
                if (parts.length == 2) {
                    parsedId = UUID.fromString(parts[1]);
                }
            } catch (DateTimeParseException | IllegalArgumentException ex) {
                throw new InvalidCursorFormatException(cursor, ex);
            }
        }
        log.debug("알림 목록 조회 - parsedTime: {}, parsedId: {}",  parsedTime, parsedId);

        Pageable pageable = PageRequest.of(
            0,
            limit + PAGE_OFFSET,
            Sort.by("createdAt").ascending()
                .and(Sort.by("id").ascending())
        );

        List<Notification> slice = notificationRepository.findPage(userId, parsedTime, parsedId, pageable);

        boolean hasNext = slice.size() > limit;
        List<Notification> pageList = hasNext
            ? slice.subList(0, limit)
            : slice;

        List<NotificationDto> content = pageList.stream()
            .map(notificationMapper::toDto)
            .toList();

        Instant nextAfter = null;
        String nextCursor = null;
        if (hasNext) {
            Notification last = pageList.get(limit - 1);
            nextAfter = last.getCreatedAt();
            nextCursor = nextAfter.toString() + "|" + last.getId().toString();
        }

        long totalUnreadNotification = notificationRepository.countByUserIdAndConfirmedFalse(userId);
        log.debug("알림 목록 조회 - totalUnreadNotification: {}", totalUnreadNotification);

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
     *     사용자 정보가 존재하지 않는 경우
     * </p>
     *
     * @param userId 확인 처리를 수행할 사용자의 ID
     * @throws UserNotFoundException 사용자를 조회할 수 없는 경우
     */
    @Override
    @Transactional
    public void confirmAll(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("userId: " + userId);
        }

        int updatedCount = notificationRepository.confirmAllByUserId(userId);
        log.info("총 {}개의 알림이 확인 처리되었습니다. (userId: {})", updatedCount, userId);
    }

    /**
     * 주어진 userId와 notificationId에 해당하는 알림을 확인된 상태로 변경합니다.
     *
     * <p>
     *     트랜잭션 내에서 Dirty Checking으로 변경된 confirmed 필드를
     *     커밋 시점에 자동으로 DB에 반영합니다.
     * </p>
     *
     * @param userId 조회할 대상 사용자의 ID
     * @param notificationId 확인 처리할 알림의 ID
     * @throws NotificationNotFoundException 알림을 찾을 수 없거나 권한이 없는 경우
     */
    @Override
    @Transactional
    public void confirmNotification(UUID userId, UUID notificationId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("userId=" + userId);
        }

        Notification notification = notificationRepository
            .findByIdAndUserId(notificationId, userId)
            .orElseThrow(() -> new NotificationNotFoundException(notificationId));

        notification.confirm();
        log.debug("notification confirmed: {}", notification);
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
}