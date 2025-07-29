package org.project.monewping.domain.useractivity.service;

import org.project.monewping.domain.useractivity.document.UserActivityDocument;
import org.project.monewping.domain.useractivity.dto.UserActivityDto;

import java.util.UUID;
import java.util.List;

/**
 * 사용자 활동 내역 관리 서비스 인터페이스
 * 
 * <p>
 * 사용자 활동 내역의 조회 및 업데이트를 담당합니다.
 * </p>
 */
public interface UserActivityService {

    /**
     * 사용자 활동 내역을 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 사용자 활동 내역 DTO
     * @throws org.project.monewping.domain.useractivity.exception.UserActivityNotFoundException
     */
    UserActivityDto getUserActivity(UUID userId);

    /**
     * 사용자 활동 내역이 존재하는지 확인합니다.
     * 
     * @param userId 사용자 ID
     * @return 존재 여부
     */
    boolean existsUserActivity(UUID userId);

    /**
     * 사용자 활동 내역을 초기화합니다.
     * 
     * @param userId        사용자 ID
     * @param userEmail     사용자 이메일
     * @param userNickname  사용자 닉네임
     * @param userCreatedAt 사용자 가입 날짜
     */
    void initializeUserActivity(UUID userId, String userEmail, String userNickname, java.time.Instant userCreatedAt);

    /**
     * 사용자 활동 내역을 삭제합니다.
     * 
     * @param userId 사용자 ID
     */
    void deleteUserActivity(UUID userId);

    /**
     * 사용자를 논리 삭제 상태로 변경합니다.
     * 사용자 정보의 isDeleted를 true로 설정하고 활동 내역은 유지합니다.
     * 
     * @param userId 사용자 ID
     */
    void softDeleteUser(UUID userId);

    /**
     * 사용자 닉네임을 활동내역에 업데이트합니다.
     * 
     * @param userId 사용자 ID
     * @param newNickname 새로운 닉네임
     */
    void updateUserNickname(UUID userId, String newNickname);

    // ========== 구독 관련 메서드 ==========

    /**
     * 구독 정보를 추가합니다.
     * 
     * @param userId           사용자 ID
     * @param subscriptionInfo 구독 정보
     */
    void addSubscription(UUID userId, UserActivityDocument.SubscriptionInfo subscriptionInfo);

    /**
     * 구독 정보를 삭제합니다.
     * 
     * @param userId     사용자 ID
     * @param interestId 관심사 ID
     */
    void removeSubscription(UUID userId, UUID interestId);

    /**
     * 사용자의 모든 구독 정보를 삭제합니다.
     * 
     * @param userId 사용자 ID
     */
    void removeAllSubscriptionsByUserId(UUID userId);

    /**
     * 관심사의 키워드 변경 시 구독 정보를 업데이트합니다.
     * 
     * @param interestId       관심사 ID
     * @param newKeywords      새로운 키워드 목록
     * @param subscriberCount  구독자 수
     */
    void updateInterestKeywords(UUID interestId, List<String> newKeywords, long subscriberCount);

    /**
     * 관심사가 삭제될 때 모든 사용자의 구독 정보에서 해당 관심사를 제거합니다.
     * 
     * @param interestId 삭제된 관심사 ID
     */
    void removeInterestFromAllSubscriptions(UUID interestId);

    // ========== 댓글 관련 메서드 ==========

    /**
     * 댓글 정보를 추가합니다.
     * 
     * @param userId      사용자 ID
     * @param commentInfo 댓글 정보
     */
    void addComment(UUID userId, UserActivityDocument.CommentInfo commentInfo);

    /**
     * 댓글 정보를 업데이트합니다.
     * 
     * @param userId     사용자 ID
     * @param commentId  댓글 ID
     * @param newContent 새로운 댓글 내용
     */
    void updateComment(UUID userId, UUID commentId, String newContent);

    /**
     * 댓글 정보를 삭제합니다.
     * 
     * @param userId    사용자 ID
     * @param commentId 댓글 ID
     */
    void removeComment(UUID userId, UUID commentId);

    // ========== 좋아요 관련 메서드 ==========

    /**
     * 댓글 좋아요 정보를 추가합니다.
     * 
     * @param userId          사용자 ID
     * @param commentLikeInfo 좋아요 정보
     */
    void addCommentLike(UUID userId, UserActivityDocument.CommentLikeInfo commentLikeInfo);

    /**
     * 댓글 좋아요 정보를 삭제합니다.
     * 
     * @param userId    사용자 ID
     * @param commentId 댓글 ID
     */
    void removeCommentLike(UUID userId, UUID commentId);

    /**
     * 사용자의 모든 댓글 좋아요를 삭제합니다.
     * 
     * @param userId 사용자 ID
     */
    void removeAllCommentLikesByUserId(UUID userId);

    /**
     * 좋아요를 누른 댓글의 내용을 업데이트합니다.
     * 
     * @param commentId  댓글 ID
     * @param newContent 새로운 댓글 내용
     */
    void updateCommentInLikes(UUID commentId, String newContent);

    // ========== 기사 조회 관련 메서드 ==========

    /**
     * 기사 조회 정보를 추가합니다.
     * 
     * @param userId          사용자 ID
     * @param articleViewInfo 기사 조회 정보
     */
    void addArticleView(UUID userId, UserActivityDocument.ArticleViewInfo articleViewInfo);
}
