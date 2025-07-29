package org.project.monewping.domain.user.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * UserDeletionRepository의 구현체
 * 사용자 삭제 시 관련된 모든 데이터를 삭제하는 기능을 제공합니다.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
@Transactional
public class UserDeletionRepositoryImpl implements UserDeletionRepository {

    private final jakarta.persistence.EntityManager entityManager;
    private final org.project.monewping.domain.useractivity.service.UserActivityService userActivityService;

    @Override
    public void deleteSubscriptionsByUserId(UUID userId) {
        try {
            // 1. 삭제될 구독 정보를 먼저 조회하여 관심사 ID 목록을 얻음
            @SuppressWarnings("unchecked")
            List<UUID> interestIds = entityManager.createQuery(
                    "SELECT s.interest.id FROM org.project.monewping.domain.interest.entity.Subscription s WHERE s.user.id = :userId")
                    .setParameter("userId", userId)
                    .getResultList();
            
            // 2. 구독 정보 삭제
            int deletedCount = entityManager.createQuery("DELETE FROM org.project.monewping.domain.interest.entity.Subscription s WHERE s.user.id = :userId")
                    .setParameter("userId", userId)
                    .executeUpdate();
            
            // 3. 각 관심사의 구독자 수 감소
            for (UUID interestId : interestIds) {
                entityManager.createQuery("UPDATE org.project.monewping.domain.interest.entity.Interest i SET i.subscriberCount = i.subscriberCount - 1 WHERE i.id = :interestId AND i.subscriberCount > 0")
                        .setParameter("interestId", interestId)
                        .executeUpdate();
            }
            
            // 4. 사용자 활동 내역의 구독 정보도 제거
            try {
                userActivityService.removeAllSubscriptionsByUserId(userId);
                log.debug("사용자 활동 내역 구독 정보 제거 완료: userId={}", userId);
            } catch (Exception e) {
                log.warn("사용자 활동 내역 구독 정보 제거 실패: userId={}, error={}", userId, e.getMessage());
            }
            
            log.debug("사용자 구독 정보 삭제 완료: userId={}, 삭제된 구독 수={}, 영향받은 관심사 수={}", 
                    userId, deletedCount, interestIds.size());
        } catch (Exception e) {
            log.error("사용자 구독 정보 삭제 실패: userId={}, error={}", userId, e.getMessage());
            throw e;
        }
    }

    @Override
    public void softDeleteCommentsByUserId(UUID userId) {
        try {
            // 1. 삭제될 댓글 ID 목록을 먼저 조회
            @SuppressWarnings("unchecked")
            List<UUID> commentIds = entityManager.createQuery(
                    "SELECT c.id FROM org.project.monewping.domain.comment.domain.Comment c WHERE c.userId = :userId AND c.isDeleted = false")
                    .setParameter("userId", userId)
                    .getResultList();
            
            // 2. 댓글 마스킹 처리
            entityManager.createQuery("UPDATE org.project.monewping.domain.comment.domain.Comment c SET c.isDeleted = true, c.content = '삭제한 사용자의 댓글입니다', c.updatedAt = CURRENT_TIMESTAMP WHERE c.userId = :userId AND c.isDeleted = false")
                    .setParameter("userId", userId)
                    .executeUpdate();
            
            // 3. 사용자 활동 내역의 댓글 정보도 마스킹 처리
            for (UUID commentId : commentIds) {
                try {
                    userActivityService.updateComment(userId, commentId, "삭제한 사용자의 댓글입니다");
                } catch (Exception e) {
                    log.warn("사용자 활동 내역 댓글 업데이트 실패: userId={}, commentId={}, error={}", userId, commentId, e.getMessage());
                }
            }
            
            log.debug("사용자 댓글 논리 삭제 완료: userId={}, 마스킹된 댓글 수={}", userId, commentIds.size());
        } catch (Exception e) {
            log.error("사용자 댓글 논리 삭제 실패: userId={}, error={}", userId, e.getMessage());
            throw e;
        }
    }

    @Override
    public void deleteCommentsByUserId(UUID userId) {
        try {
            entityManager.createQuery("DELETE FROM org.project.monewping.domain.comment.domain.Comment c WHERE c.userId = :userId")
                    .setParameter("userId", userId)
                    .executeUpdate();
            log.debug("사용자 댓글 물리 삭제 완료: userId={}", userId);
        } catch (Exception e) {
            log.error("사용자 댓글 물리 삭제 실패: userId={}, error={}", userId, e.getMessage());
            throw e;
        }
    }

    @Override
    public void deleteCommentLikesByUserId(UUID userId) {
        try {
            // 1. 삭제될 좋아요 정보를 먼저 조회하여 댓글 ID 목록을 얻음
            @SuppressWarnings("unchecked")
            List<UUID> commentIds = entityManager.createQuery(
                    "SELECT cl.comment.id FROM org.project.monewping.domain.comment.domain.CommentLike cl WHERE cl.user.id = :userId")
                    .setParameter("userId", userId)
                    .getResultList();
            
            // 2. 좋아요 정보 삭제
            int deletedCount = entityManager.createQuery("DELETE FROM org.project.monewping.domain.comment.domain.CommentLike cl WHERE cl.user.id = :userId")
                    .setParameter("userId", userId)
                    .executeUpdate();
            
            // 3. 각 댓글의 좋아요 수 감소
            for (UUID commentId : commentIds) {
                entityManager.createQuery("UPDATE org.project.monewping.domain.comment.domain.Comment c SET c.likeCount = c.likeCount - 1 WHERE c.id = :commentId AND c.likeCount > 0")
                        .setParameter("commentId", commentId)
                        .executeUpdate();
            }
            
            log.debug("사용자 댓글 좋아요 삭제 완료: userId={}, 삭제된 좋아요 수={}, 영향받은 댓글 수={}", 
                    userId, deletedCount, commentIds.size());
        } catch (Exception e) {
            log.error("사용자 댓글 좋아요 삭제 실패: userId={}, error={}", userId, e.getMessage());
            throw e;
        }
    }

    @Override
    public void deleteNotificationsByUserId(UUID userId) {
        try {
            entityManager.createQuery("DELETE FROM org.project.monewping.domain.notification.entity.Notification n WHERE n.userId = :userId")
                    .setParameter("userId", userId)
                    .executeUpdate();
            log.debug("사용자 알림 삭제 완료: userId={}", userId);
        } catch (Exception e) {
            log.error("사용자 알림 삭제 실패: userId={}, error={}", userId, e.getMessage());
            throw e;
        }
    }

    @Override
    public void deleteArticleViewsByUserId(UUID userId) {
        try {
            // 1. 삭제될 조회 기록을 먼저 조회하여 기사 ID 목록을 얻음
            @SuppressWarnings("unchecked")
            List<UUID> articleIds = entityManager.createQuery(
            "SELECT av.article.id FROM org.project.monewping.domain.article.entity.ArticleViews av WHERE av.viewedBy = :userId")
                    .setParameter("userId", userId)
                    .getResultList();
            
            // 2. 조회 기록 삭제
            int deletedCount = entityManager.createQuery("DELETE FROM org.project.monewping.domain.article.entity.ArticleViews av WHERE av.viewedBy = :userId")
                    .setParameter("userId", userId)
                    .executeUpdate();
            
            // 3. 각 기사의 조회수 감소
            for (UUID articleId : articleIds) {
                entityManager.createQuery("UPDATE org.project.monewping.domain.article.entity.Articles a SET a.viewCount = a.viewCount - 1 WHERE a.id = :articleId AND a.viewCount > 0")
                        .setParameter("articleId", articleId)
                        .executeUpdate();
            }
            
            log.debug("사용자 기사 조회 기록 삭제 완료: userId={}, 삭제된 조회 기록 수={}, 영향받은 기사 수={}", 
                    userId, deletedCount, articleIds.size());
        } catch (Exception e) {
            log.error("사용자 기사 조회 기록 삭제 실패: userId={}, error={}", userId, e.getMessage());
            throw e;
        }
    }
} 