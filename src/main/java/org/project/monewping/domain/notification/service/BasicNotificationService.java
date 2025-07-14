package org.project.monewping.domain.notification.service;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.ArrayList;
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
     * 리소스 별 알림을 생성합니다. (기능 통합 전 테스트 로직)
     *
     * <p>resourceTyep 값에 따라 알림 분기 처리를 진행합니다.</p>
     *
     * <p>
     * <strong>※ 추후 기능 통합 시 아래 로직으로 대체될 예정입니다.</strong>
     * <ul>
     *   <li><strong>Article</strong>:
     *     <ol>
     *       <li>게시글(article)로부터 관심사(interestId)를 조회</li>
     *       <li>해당 관심사에 구독한 사용자 목록(subscriberIds) 조회</li>
     *       <li>가장 최근 알림 이후 생성된 기사 수(count) 집계</li>
     *       <li>구독자 별로 "{interestName}와 관련된 기사가 {count}건 등록되었습니다." 메시지 생성</li>
     *     </ol>
     *   </li>
     *   <li><strong>Comment</strong>:
     *     <ol>
     *       <li>댓글 작성자 닉네임 조회</li>
     *       <li>"{user}님이 나의 댓글을 좋아합니다." 메시지 생성</li>
     *     </ol>
     *   </li>
     * </ul>
     * </p>
     *
     * @param userId 알림 대상자의 ID
     * @param resourceId  알림 대상 리소스의 ID
     * @param resourceType 알림의 리소스 타입 ("Article", "Comment")
     * @return 생성/저장된 NotificationDto 리스트
     */
    @Override
    @Transactional
    public List<NotificationDto> create(UUID userId, UUID resourceId, String resourceType) {
        log.debug("Creating notifications for user {} and resource {}, resourceType {}", userId, resourceId, resourceType);
        List<Notification> notifications = new ArrayList<>();

        switch (resourceType) {
            case "Article": {
//              Article article = articleRepository.findById(resourceId);
//              UUID interestId = article.getInterest().getId();
//
//              String interestName = interestRepository.findById(interestId).getName();
//
//                List<UUID> subscriberIds = interestSubscriptionRepository.findUserIdsByInterestId(interestId);
//
//                long count = articleRepository
//                    .countByInterestIdAndCreatedAtAfter(interestId, lastNotifiedAt);
//
//              notifications = subscriberIds.stream()
//                  .map(userId -> {
//                      String content = String.format(
//                          "%s와 관련된 기사가 %d건 등록되었습니다.",
//                          interestName,
//                            count
//                      );
//                        return new Notification(userId, content, resourceId,"Article");
//                  })
//                    .collect(Collectors.toList());
//
//              notificationRepository.saveAll(notifications);

                String testInterestName = "스포츠";
                int testCount = 2;
                String testContent = testInterestName + "와 관련된 기사가 " + testCount + "건 등록되었습니다.";

                Notification notification = new Notification(
                    userId, testContent, resourceId, resourceType
                );
                notificationRepository.save(notification);
                notifications.add(notification);
                break;
            }
            case "Comment": {
                //String user = userRepository.findById(resourceId).getNickname();

                String testUser = "User";
                String testContent = testUser + "님이 나의 댓글을 좋아합니다.";

                Notification notification = new Notification(
                    userId, testContent, resourceId, resourceType
                );
                notificationRepository.save(notification);
                notifications.add(notification);
                break;
            }
            default:
                throw new UnsupportedResourceTypeException(resourceType);
        }

        return notifications.stream()
            .map(notificationMapper::toDto)
            .collect(Collectors.toList());
    }
}