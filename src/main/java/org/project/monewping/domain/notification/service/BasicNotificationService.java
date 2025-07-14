package org.project.monewping.domain.notification.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.monewping.domain.notification.dto.NotificationDto;
import org.project.monewping.domain.notification.entity.Notification;
import org.project.monewping.domain.notification.exception.UnsupportedResourceTypeException;
import org.project.monewping.domain.notification.mapper.NotificationMapper;
import org.project.monewping.domain.notification.repository.NotificationRepository;
import org.project.monewping.global.dto.CursorPageResponse;
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

    @Override
    public CursorPageResponse<NotificationDto> findNotifications(UUID userId, Instant after, int limit) {
        return notificationRepository.findByUserIdAndAfter(userId, after, limit);
    }


    /**
     * 관심사의 새로운 기사에 대한 알림을 생성합니다. (기능 통합 전 테스트 로직)
     *
     * <strong>※ 추후 기능 통합 시 아래 로직으로 대체될 예정입니다.</strong>
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
     * <strong>※ 추후 기능 통합 시 로직이 수정될 예정입니다.</strong>
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