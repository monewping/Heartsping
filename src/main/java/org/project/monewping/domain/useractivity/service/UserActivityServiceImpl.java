package org.project.monewping.domain.useractivity.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.monewping.domain.useractivity.document.UserActivityDocument;
import org.project.monewping.domain.useractivity.dto.UserActivityDto;
import org.project.monewping.domain.useractivity.exception.UserActivityNotFoundException;
import org.project.monewping.domain.useractivity.mapper.UserActivityMapper;
import org.project.monewping.domain.useractivity.repository.UserActivityRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 사용자 활동 내역 관리 서비스 구현체
 * 
 * <p>
 * 사용자 활동 내역의 조회 및 업데이트를 담당합니다.
 * MongoDB를 사용하여 역정규화된 데이터를 관리합니다.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserActivityServiceImpl implements UserActivityService {

    private final UserActivityRepository userActivityRepository;
    private final UserActivityMapper userActivityMapper;

    private static final int MAX_ACTIVITY_COUNT = 10;

    @Override
    public UserActivityDto getUserActivity(UUID userId) {
        log.debug("사용자 활동 내역 조회 시작. userId: {}", userId);

        UserActivityDocument document = userActivityRepository.findByUserId(userId)
                .orElseThrow(() -> new UserActivityNotFoundException(userId));

        UserActivityDto dto = userActivityMapper.toDto(document);

        log.debug("사용자 활동 내역 조회 완료. userId: {}", userId);
        return dto;
    }

    @Override
    public boolean existsUserActivity(UUID userId) {
        return userActivityRepository.existsByUserId(userId);
    }

    @Override
    @Transactional
    public void initializeUserActivity(UUID userId, String userEmail, String userNickname, Instant userCreatedAt) {
        log.debug("사용자 활동 내역 초기화 시작. userId: {}", userId);

        if (userActivityRepository.existsByUserId(userId)) {
            log.debug("이미 사용자 활동 내역이 존재합니다. userId: {}", userId);
            return;
        }

        UserActivityDocument.UserInfo userInfo = UserActivityDocument.UserInfo.builder()
                .id(userId)
                .email(userEmail)
                .nickname(userNickname)
                .createdAt(userCreatedAt)
                .build();

        UserActivityDocument document = UserActivityDocument.builder()
                .userId(userId)
                .user(userInfo)
                .subscriptions(new ArrayList<>())
                .comments(new ArrayList<>())
                .commentLikes(new ArrayList<>())
                .articleViews(new ArrayList<>())
                .updatedAt(Instant.now())
                .build();

        userActivityRepository.save(document);

        log.debug("사용자 활동 내역 초기화 완료. userId: {}", userId);
    }

    @Override
    @Transactional
    public void deleteUserActivity(UUID userId) {
        log.debug("사용자 활동 내역 삭제 시작. userId: {}", userId);

        userActivityRepository.deleteByUserId(userId);

        log.debug("사용자 활동 내역 삭제 완료. userId: {}", userId);
    }

    // ========== 구독 관련 메서드 ==========

    @Override
    @Transactional
    public void addSubscription(UUID userId, UserActivityDocument.SubscriptionInfo subscriptionInfo) {
        log.debug("구독 정보 추가 시작. userId: {}, interestId: {}", userId, subscriptionInfo.getInterestId());

        // 기존 구독 목록 조회
        UserActivityDocument document = userActivityRepository.findByUserId(userId)
                .orElseThrow(() -> new UserActivityNotFoundException(userId));

        List<UserActivityDocument.SubscriptionInfo> subscriptions = document.getSubscriptions();
        if (subscriptions == null) {
            subscriptions = new ArrayList<>();
        }

        // 새 구독 추가
        subscriptions.add(0, subscriptionInfo); // 맨 앞에 추가

        // 최대 10개만 유지
        if (subscriptions.size() > MAX_ACTIVITY_COUNT) {
            subscriptions = subscriptions.subList(0, MAX_ACTIVITY_COUNT);
        }

        // 업데이트
        document.setSubscriptions(subscriptions);
        document.setUpdatedAt(Instant.now());
        userActivityRepository.save(document);

        log.debug("구독 정보 추가 완료. userId: {}, interestId: {}", userId, subscriptionInfo.getInterestId());
    }

    @Override
    @Transactional
    public void removeSubscription(UUID userId, UUID interestId) {
        log.debug("구독 정보 삭제 시작. userId: {}, interestId: {}", userId, interestId);

        // 기존 구독 목록 조회
        UserActivityDocument document = userActivityRepository.findByUserId(userId)
                .orElseThrow(() -> new UserActivityNotFoundException(userId));

        List<UserActivityDocument.SubscriptionInfo> subscriptions = document.getSubscriptions();
        if (subscriptions != null) {
            // 해당 관심사 구독 제거
            subscriptions.removeIf(subscription -> subscription.getInterestId().equals(interestId));
            document.setSubscriptions(subscriptions);
            document.setUpdatedAt(Instant.now());
            userActivityRepository.save(document);
        }

        log.debug("구독 정보 삭제 완료. userId: {}, interestId: {}", userId, interestId);
    }

    @Override
    @Transactional
    public void updateInterestKeywords(UUID interestId, List<String> newKeywords, long subscriberCount) {
        log.debug("관심사 키워드 업데이트 시작. interestId: {}, newKeywords: {}", interestId, newKeywords);

        // 해당 관심사를 구독한 모든 사용자의 활동 내역을 업데이트
        List<UserActivityDocument> documentsToUpdate = userActivityRepository.findAll().stream()
                .filter(doc -> doc.getSubscriptions() != null && 
                    doc.getSubscriptions().stream()
                        .anyMatch(sub -> sub.getInterestId().equals(interestId)))
                .toList();

        for (UserActivityDocument document : documentsToUpdate) {
            List<UserActivityDocument.SubscriptionInfo> subscriptions = document.getSubscriptions();
            boolean updated = false;

            for (UserActivityDocument.SubscriptionInfo subscription : subscriptions) {
                if (subscription.getInterestId().equals(interestId)) {
                    subscription.setInterestKeywords(newKeywords);
                    subscription.setInterestSubscriberCount(subscriberCount);
                    updated = true;
                }
            }

            if (updated) {
                document.setSubscriptions(subscriptions);
                document.setUpdatedAt(Instant.now());
                userActivityRepository.save(document);
                log.debug("사용자 활동 내역의 관심사 키워드 업데이트 완료. userId: {}, interestId: {}", 
                    document.getUserId(), interestId);
            }
        }

        log.debug("관심사 키워드 업데이트 완료. interestId: {}, 업데이트된 문서 수: {}", 
            interestId, documentsToUpdate.size());
    }

    @Override
    @Transactional
    public void removeInterestFromAllSubscriptions(UUID interestId) {
        log.debug("모든 사용자의 구독 목록에서 관심사 제거 시작. interestId: {}", interestId);

        // 해당 관심사를 구독한 모든 사용자의 활동 내역을 조회
        List<UserActivityDocument> documentsToUpdate = userActivityRepository.findAll().stream()
                .filter(doc -> doc.getSubscriptions() != null && 
                    doc.getSubscriptions().stream()
                        .anyMatch(sub -> sub.getInterestId().equals(interestId)))
                .toList();

        for (UserActivityDocument document : documentsToUpdate) {
            List<UserActivityDocument.SubscriptionInfo> subscriptions = document.getSubscriptions();
            
            // 해당 관심사의 구독 정보 제거
            boolean removed = subscriptions.removeIf(sub -> sub.getInterestId().equals(interestId));
            
            if (removed) {
                document.setSubscriptions(subscriptions);
                document.setUpdatedAt(Instant.now());
                userActivityRepository.save(document);
                log.debug("사용자 활동 내역에서 관심사 구독 정보 제거 완료. userId: {}, interestId: {}", 
                    document.getUserId(), interestId);
            }
        }

        log.debug("모든 사용자의 구독 목록에서 관심사 제거 완료. interestId: {}, 업데이트된 문서 수: {}", 
            interestId, documentsToUpdate.size());
    }

    // ========== 댓글 관련 메서드 ==========

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void addComment(UUID userId, UserActivityDocument.CommentInfo commentInfo) {
        log.debug("댓글 정보 추가 시작. userId: {}, commentId: {}", userId, commentInfo.getId());

        // 기존 댓글 목록 조회
        UserActivityDocument document = userActivityRepository.findByUserId(userId)
                .orElseThrow(() -> new UserActivityNotFoundException(userId));

        List<UserActivityDocument.CommentInfo> comments = document.getComments();
        if (comments == null) {
            comments = new ArrayList<>();
        }

        // 새 댓글 추가
        comments.add(0, commentInfo); // 맨 앞에 추가

        // 최대 10개만 유지
        if (comments.size() > MAX_ACTIVITY_COUNT) {
            comments = comments.subList(0, MAX_ACTIVITY_COUNT);
        }

        // 업데이트
        document.setComments(comments);
        document.setUpdatedAt(Instant.now());
        userActivityRepository.save(document);

        log.debug("댓글 정보 추가 완료. userId: {}, commentId: {}", userId, commentInfo.getId());
    }

    @Override
    @Transactional
    public void updateComment(UUID userId, UUID commentId, String newContent) {
        log.debug("댓글 정보 업데이트 시작. userId: {}, commentId: {}", userId, commentId);

        // 기존 댓글 목록 조회
        UserActivityDocument document = userActivityRepository.findByUserId(userId)
                .orElseThrow(() -> new UserActivityNotFoundException(userId));

        List<UserActivityDocument.CommentInfo> comments = document.getComments();
        if (comments != null) {
            // 해당 댓글 찾아서 내용 업데이트
            for (UserActivityDocument.CommentInfo comment : comments) {
                if (comment.getId().equals(commentId)) {
                    comment.setContent(newContent);
                    break;
                }
            }
            document.setComments(comments);
            document.setUpdatedAt(Instant.now());
            userActivityRepository.save(document);
        }

        log.debug("댓글 정보 업데이트 완료. userId: {}, commentId: {}", userId, commentId);
    }

    @Override
    @Transactional
    public void removeComment(UUID userId, UUID commentId) {
        log.debug("댓글 정보 삭제 시작. userId: {}, commentId: {}", userId, commentId);

        // 기존 댓글 목록 조회
        UserActivityDocument document = userActivityRepository.findByUserId(userId)
                .orElseThrow(() -> new UserActivityNotFoundException(userId));

        List<UserActivityDocument.CommentInfo> comments = document.getComments();
        if (comments != null) {
            // 해당 댓글 제거
            comments.removeIf(comment -> comment.getId().equals(commentId));
            document.setComments(comments);
            document.setUpdatedAt(Instant.now());
            userActivityRepository.save(document);
        }

        log.debug("댓글 정보 삭제 완료. userId: {}, commentId: {}", userId, commentId);
    }

    // ========== 좋아요 관련 메서드 ==========

    @Override
    @Transactional
    public void addCommentLike(UUID userId, UserActivityDocument.CommentLikeInfo commentLikeInfo) {
        log.debug("댓글 좋아요 정보 추가 시작. userId: {}, commentId: {}", userId, commentLikeInfo.getCommentId());

        // 기존 좋아요 목록 조회
        UserActivityDocument document = userActivityRepository.findByUserId(userId)
                .orElseThrow(() -> new UserActivityNotFoundException(userId));

        List<UserActivityDocument.CommentLikeInfo> commentLikes = document.getCommentLikes();
        if (commentLikes == null) {
            commentLikes = new ArrayList<>();
        }

        // 새 좋아요 추가
        commentLikes.add(0, commentLikeInfo); // 맨 앞에 추가

        // 최대 10개만 유지
        if (commentLikes.size() > MAX_ACTIVITY_COUNT) {
            commentLikes = commentLikes.subList(0, MAX_ACTIVITY_COUNT);
        }

        // 업데이트
        document.setCommentLikes(commentLikes);
        document.setUpdatedAt(Instant.now());
        userActivityRepository.save(document);

        log.debug("댓글 좋아요 정보 추가 완료. userId: {}, commentId: {}", userId, commentLikeInfo.getCommentId());
    }

    @Override
    @Transactional
    public void removeCommentLike(UUID userId, UUID commentId) {
        log.debug("댓글 좋아요 정보 삭제 시작. userId: {}, commentId: {}", userId, commentId);

        // 기존 좋아요 목록 조회
        UserActivityDocument document = userActivityRepository.findByUserId(userId)
                .orElseThrow(() -> new UserActivityNotFoundException(userId));

        List<UserActivityDocument.CommentLikeInfo> commentLikes = document.getCommentLikes();
        if (commentLikes != null) {
            // 해당 좋아요 제거
            commentLikes.removeIf(like -> like.getCommentId().equals(commentId));
            document.setCommentLikes(commentLikes);
            document.setUpdatedAt(Instant.now());
            userActivityRepository.save(document);
        }

        log.debug("댓글 좋아요 정보 삭제 완료. userId: {}, commentId: {}", userId, commentId);
    }

    @Override
    @Transactional
    public void updateCommentInLikes(UUID commentId, String newContent) {
        log.debug("좋아요를 누른 댓글 내용 업데이트 시작. commentId: {}", commentId);

        // 해당 댓글을 좋아요한 모든 사용자의 활동 내역을 업데이트
        List<UserActivityDocument> documentsToUpdate = userActivityRepository.findAll().stream()
                .filter(doc -> doc.getCommentLikes() != null && 
                        doc.getCommentLikes().stream()
                            .anyMatch(like -> like.getCommentId().equals(commentId)))
                .toList();

        for (UserActivityDocument document : documentsToUpdate) {
            List<UserActivityDocument.CommentLikeInfo> commentLikes = document.getCommentLikes();
            boolean updated = false;
            
            for (UserActivityDocument.CommentLikeInfo commentLike : commentLikes) {
                if (commentLike.getCommentId().equals(commentId)) {
                    commentLike.setCommentContent(newContent);
                    updated = true;
                }
            }
            
            if (updated) {
                document.setCommentLikes(commentLikes);
                document.setUpdatedAt(Instant.now());
                userActivityRepository.save(document);
                log.debug("사용자 활동 내역의 댓글 좋아요 정보 업데이트 완료. userId: {}, commentId: {}", 
                    document.getUserId(), commentId);
            }
        }

        log.debug("좋아요를 누른 댓글 내용 업데이트 완료. commentId: {}, 업데이트된 문서 수: {}", 
            commentId, documentsToUpdate.size());
    }

    // ========== 기사 조회 관련 메서드 ==========

    @Override
    @Transactional
    public void addArticleView(UUID userId, UserActivityDocument.ArticleViewInfo articleViewInfo) {
        log.debug("기사 조회 정보 추가 시작. userId: {}, articleId: {}", userId, articleViewInfo.getArticleId());

        // 기존 조회 목록 조회
        UserActivityDocument document = userActivityRepository.findByUserId(userId)
                .orElseThrow(() -> new UserActivityNotFoundException(userId));

        List<UserActivityDocument.ArticleViewInfo> articleViews = document.getArticleViews();
        if (articleViews == null) {
            articleViews = new ArrayList<>();
        }

        // 새 조회 추가
        articleViews.add(0, articleViewInfo); // 맨 앞에 추가

        // 최대 10개만 유지
        if (articleViews.size() > MAX_ACTIVITY_COUNT) {
            articleViews = articleViews.subList(0, MAX_ACTIVITY_COUNT);
        }

        // 업데이트
        document.setArticleViews(articleViews);
        document.setUpdatedAt(Instant.now());
        userActivityRepository.save(document);

        log.debug("기사 조회 정보 추가 완료. userId: {}, articleId: {}", userId, articleViewInfo.getArticleId());
    }
}