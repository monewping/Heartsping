package org.project.monewping.domain.user.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.project.monewping.domain.article.entity.Articles;
import org.project.monewping.domain.article.entity.ArticleViews;
import org.project.monewping.domain.comment.domain.Comment;
import org.project.monewping.domain.comment.domain.CommentLike;
import org.project.monewping.domain.interest.entity.Interest;
import org.project.monewping.domain.interest.entity.Subscription;
import org.project.monewping.domain.notification.entity.Notification;
import org.project.monewping.domain.user.domain.User;
import org.project.monewping.domain.useractivity.service.UserActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserDeletionRepositoryImplTest {

    @Autowired
    private UserDeletionRepositoryImpl userDeletionRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserActivityService userActivityService;

    private User testUser;
    private Interest testInterest;
    private Articles testArticle;
    private Comment testComment;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        testUser = User.builder()
                .email("test@example.com")
                .nickname("테스트유저")
                .password("password123")
                .isDeleted(false)
                .build();
        entityManager.persist(testUser);

        // 테스트 관심사 생성
        testInterest = Interest.builder()
                .name("테스트 관심사")
                .subscriberCount(0L)
                .build();
        entityManager.persist(testInterest);

        // 테스트 기사 생성
        testArticle = Articles.builder()
                .title("테스트 기사")
                .summary("테스트 내용")
                .source("테스트")
                .originalLink("http://test.com")
                .publishedAt(java.time.LocalDateTime.now())
                .commentCount(0L)
                .viewCount(0L)
                .deleted(false)
                .interest(testInterest)
                .build();
        entityManager.persist(testArticle);

        // 테스트 댓글 생성
        testComment = Comment.builder()
                .articleId(testArticle.getId())
                .userId(testUser.getId())
                .userNickname("테스트유저")
                .content("테스트 댓글")
                .likeCount(0)
                .createdAt(java.time.Instant.now())
                .updatedAt(java.time.Instant.now())
                .isDeleted(false)
                .build();
        entityManager.persist(testComment);

        entityManager.flush();
    }

    @Test
    @DisplayName("사용자 구독 정보 삭제 테스트")
    void deleteSubscriptionsByUserId_Success() {
        // given
        Subscription subscription = new Subscription(testUser, testInterest);
        entityManager.persist(subscription);
        
        testInterest.increaseSubscriber();
        entityManager.merge(testInterest);
        
        entityManager.flush();

        // when
        userDeletionRepository.deleteSubscriptionsByUserId(testUser.getId());

        // then
        entityManager.clear();
        
        Interest foundInterest = entityManager.find(Interest.class, testInterest.getId());
        assertThat(foundInterest.getSubscriberCount()).isEqualTo(0L);
        
        Subscription foundSubscription = entityManager.find(Subscription.class, subscription.getId());
        assertThat(foundSubscription).isNull();
    }

    @Test
    @DisplayName("사용자 댓글 논리 삭제 테스트")
    void softDeleteCommentsByUserId_Success() {
        // given
        Comment comment2 = Comment.builder()
                .articleId(testArticle.getId())
                .userId(testUser.getId())
                .userNickname("테스트유저")
                .content("테스트 댓글 2")
                .likeCount(0)
                .createdAt(java.time.Instant.now())
                .updatedAt(java.time.Instant.now())
                .isDeleted(false)
                .build();
        entityManager.persist(comment2);
        entityManager.flush();

        // when
        userDeletionRepository.softDeleteCommentsByUserId(testUser.getId());

        // then
        entityManager.clear();
        
        Comment foundComment1 = entityManager.find(Comment.class, testComment.getId());
        assertThat(foundComment1.isDeleted()).isTrue();
        assertThat(foundComment1.getContent()).isEqualTo("삭제한 사용자의 댓글입니다");
        
        Comment foundComment2 = entityManager.find(Comment.class, comment2.getId());
        assertThat(foundComment2.isDeleted()).isTrue();
        assertThat(foundComment2.getContent()).isEqualTo("삭제한 사용자의 댓글입니다");
    }

    @Test
    @DisplayName("사용자 댓글 물리 삭제 테스트")
    void deleteCommentsByUserId_Success() {
        // given
        Comment comment2 = Comment.builder()
                .articleId(testArticle.getId())
                .userId(testUser.getId())
                .userNickname("테스트유저")
                .content("테스트 댓글 2")
                .likeCount(0)
                .createdAt(java.time.Instant.now())
                .updatedAt(java.time.Instant.now())
                .isDeleted(false)
                .build();
        entityManager.persist(comment2);
        entityManager.flush();

        // when
        userDeletionRepository.deleteCommentsByUserId(testUser.getId());

        // then
        entityManager.clear();
        
        Comment foundComment1 = entityManager.find(Comment.class, testComment.getId());
        assertThat(foundComment1).isNull();
        
        Comment foundComment2 = entityManager.find(Comment.class, comment2.getId());
        assertThat(foundComment2).isNull();
    }

    @Test
    @DisplayName("사용자 댓글 좋아요 삭제 테스트")
    void deleteCommentLikesByUserId_Success() {
        // given
        CommentLike commentLike = CommentLike.builder()
                .comment(testComment)
                .user(testUser)
                .build();
        entityManager.persist(commentLike);
        
        entityManager.flush();

        // when
        userDeletionRepository.deleteCommentLikesByUserId(testUser.getId());

        // then
        entityManager.clear();
        
        CommentLike foundCommentLike = entityManager.find(CommentLike.class, commentLike.getId());
        assertThat(foundCommentLike).isNull();
        
        Comment foundComment = entityManager.find(Comment.class, testComment.getId());
        assertThat(foundComment.getLikeCount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("사용자 알림 삭제 테스트")
    void deleteNotificationsByUserId_Success() {
        // given
        Notification notification = Notification.builder()
                .userId(testUser.getId())
                .content("테스트 알림")
                .resourceType("TEST")
                .resourceId(UUID.randomUUID())
                .confirmed(false)
                .build();
        entityManager.persist(notification);
        entityManager.flush();

        // when
        userDeletionRepository.deleteNotificationsByUserId(testUser.getId());

        // then
        entityManager.clear();
        
        Notification foundNotification = entityManager.find(Notification.class, notification.getId());
        assertThat(foundNotification).isNull();
    }

    @Test
    @DisplayName("사용자 기사 조회 기록 삭제 테스트")
    void deleteArticleViewsByUserId_Success() {
        // given
        ArticleViews articleView = ArticleViews.builder()
                .viewedBy(testUser.getId())
                .article(testArticle)
                .createdAt(java.time.LocalDateTime.now())
                .build();
        entityManager.persist(articleView);
        
        testArticle.increaseViewCount();
        entityManager.merge(testArticle);
        
        entityManager.flush();

        // when
        userDeletionRepository.deleteArticleViewsByUserId(testUser.getId());

        // then
        entityManager.clear();
        
        ArticleViews foundArticleView = entityManager.find(ArticleViews.class, articleView.getId());
        assertThat(foundArticleView).isNull();
        
        Articles foundArticle = entityManager.find(Articles.class, testArticle.getId());
        assertThat(foundArticle.getViewCount()).isEqualTo(0L);
    }
} 