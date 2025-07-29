package org.project.monewping.domain.user.repository;

import java.util.UUID;

/**
 * 사용자 삭제 기능에서만 사용하는 리포지토리 메서드들을 정의합니다.
 * 다른 도메인의 테스트에 영향을 주지 않도록 별도 인터페이스로 분리했습니다.
 */
public interface UserDeletionRepository {

    /**
     * 사용자의 모든 구독을 삭제합니다.
     *
     * @param userId 삭제할 사용자 ID
     */
    void deleteSubscriptionsByUserId(UUID userId);

    /**
     * 사용자의 모든 댓글을 논리적으로 삭제합니다.
     *
     * @param userId 삭제할 사용자 ID
     */
    void softDeleteCommentsByUserId(UUID userId);

    /**
     * 사용자의 모든 댓글을 물리적으로 삭제합니다.
     *
     * @param userId 삭제할 사용자 ID
     */
    void deleteCommentsByUserId(UUID userId);

    /**
     * 사용자의 모든 댓글 좋아요를 삭제합니다.
     *
     * @param userId 삭제할 사용자 ID
     */
    void deleteCommentLikesByUserId(UUID userId);

    /**
     * 사용자의 모든 알림을 삭제합니다.
     *
     * @param userId 삭제할 사용자 ID
     */
    void deleteNotificationsByUserId(UUID userId);

    /**
     * 사용자의 모든 기사 조회 기록을 삭제합니다.
     *
     * @param userId 삭제할 사용자 ID
     */
    void deleteArticleViewsByUserId(UUID userId);
} 