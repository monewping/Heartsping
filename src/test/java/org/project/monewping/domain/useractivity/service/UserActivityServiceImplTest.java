package org.project.monewping.domain.useractivity.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.monewping.domain.useractivity.document.UserActivityDocument;
import org.project.monewping.domain.useractivity.dto.UserActivityDto;
import org.project.monewping.domain.useractivity.exception.UserActivityNotFoundException;
import org.project.monewping.domain.useractivity.mapper.UserActivityMapper;
import org.project.monewping.domain.useractivity.repository.UserActivityRepository;

/**
 * UserActivityServiceImpl의 단위 테스트
 * 
 * <p>
 * MongoDB 의존성을 모킹하여 순수한 비즈니스 로직만을 테스트합니다.
 * 사용자 활동 내역의 초기화, 구독, 댓글, 좋아요, 기사 조회 등의 모든 기능을 검증합니다.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserActivityService 단위 테스트")
class UserActivityServiceImplTest {

    @Mock
    private UserActivityRepository userActivityRepository;

    @Mock
    private UserActivityMapper userActivityMapper;

    @InjectMocks
    private UserActivityServiceImpl userActivityService;

    private UUID testUserId;
    private String testUserEmail;
    private String testUserNickname;
    private Instant testUserCreatedAt;
    private UserActivityDocument testDocument;
    private UserActivityDto testDto;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUserEmail = "test@example.com";
        testUserNickname = "테스트유저";
        testUserCreatedAt = Instant.now();

        UserActivityDocument.UserInfo userInfo = UserActivityDocument.UserInfo.builder()
                .id(testUserId)
                .email(testUserEmail)
                .nickname(testUserNickname)
                .createdAt(testUserCreatedAt)
                .build();

        testDocument = UserActivityDocument.builder()
                .userId(testUserId)
                .user(userInfo)
                .subscriptions(new ArrayList<>())
                .comments(new ArrayList<>())
                .commentLikes(new ArrayList<>())
                .articleViews(new ArrayList<>())
                .updatedAt(Instant.now())
                .build();

        testDto = UserActivityDto.builder()
                .id(testUserId)
                .email(testUserEmail)
                .nickname(testUserNickname)
                .createdAt(testUserCreatedAt)
                .subscriptions(new ArrayList<>())
                .comments(new ArrayList<>())
                .commentLikes(new ArrayList<>())
                .articleViews(new ArrayList<>())
                .updatedAt(Instant.now())
                .build();
    }

    // ========== 초기화 관련 테스트 ==========

    @Test
    @DisplayName("사용자 활동 내역 초기화 성공")
    void initializeUserActivity_Success() {
        // given
        given(userActivityRepository.existsByUserId(testUserId)).willReturn(false);
        given(userActivityRepository.save(any(UserActivityDocument.class))).willReturn(testDocument);

        // when
        userActivityService.initializeUserActivity(testUserId, testUserEmail, testUserNickname, testUserCreatedAt);

        // then
        verify(userActivityRepository).existsByUserId(testUserId);
        verify(userActivityRepository).save(any(UserActivityDocument.class));
    }

    @Test
    @DisplayName("사용자 활동 내역 초기화 - 이미 존재하는 경우 무시")
    void initializeUserActivity_AlreadyExists() {
        // given
        given(userActivityRepository.existsByUserId(testUserId)).willReturn(true);

        // when
        userActivityService.initializeUserActivity(testUserId, testUserEmail, testUserNickname, testUserCreatedAt);

        // then
        verify(userActivityRepository).existsByUserId(testUserId);
        verify(userActivityRepository, never()).save(any(UserActivityDocument.class));
    }

    @Test
    @DisplayName("사용자 활동 내역 조회 성공")
    void getUserActivity_Success() {
        // given
        given(userActivityRepository.findByUserId(testUserId)).willReturn(Optional.of(testDocument));
        given(userActivityMapper.toDto(testDocument)).willReturn(testDto);

        // when
        UserActivityDto result = userActivityService.getUserActivity(testUserId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testUserId);
        verify(userActivityRepository).findByUserId(testUserId);
        verify(userActivityMapper).toDto(testDocument);
    }

    @Test
    @DisplayName("사용자 활동 내역 조회 실패 - 존재하지 않는 사용자")
    void getUserActivity_NotFound() {
        // given
        given(userActivityRepository.findByUserId(testUserId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userActivityService.getUserActivity(testUserId))
                .isInstanceOf(UserActivityNotFoundException.class);

        verify(userActivityRepository).findByUserId(testUserId);
        verify(userActivityMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("사용자 활동 내역 삭제 성공")
    void deleteUserActivity_Success() {
        // when
        userActivityService.deleteUserActivity(testUserId);

        // then
        verify(userActivityRepository).deleteByUserId(testUserId);
    }

    // ========== 구독 관련 테스트 ==========

    @Test
    @DisplayName("구독 정보 추가 성공")
    void addSubscription_Success() {
        // given
        UUID interestId = UUID.randomUUID();
        String interestName = "테스트 관심사";
        List<String> keywords = Arrays.asList("키워드1", "키워드2");
        long subscriberCount = 100L;

        UserActivityDocument.SubscriptionInfo subscriptionInfo = UserActivityDocument.SubscriptionInfo.builder()
                .id(UUID.randomUUID())
                .interestId(interestId)
                .interestName(interestName)
                .interestKeywords(keywords)
                .interestSubscriberCount(subscriberCount)
                .createdAt(Instant.now())
                .build();

        given(userActivityRepository.findByUserId(testUserId)).willReturn(Optional.of(testDocument));
        given(userActivityRepository.save(any(UserActivityDocument.class))).willReturn(testDocument);

        // when
        userActivityService.addSubscription(testUserId, subscriptionInfo);

        // then
        verify(userActivityRepository).findByUserId(testUserId);
        verify(userActivityRepository).save(any(UserActivityDocument.class));
        assertThat(testDocument.getSubscriptions()).hasSize(1);
        assertThat(testDocument.getSubscriptions().get(0).getInterestId()).isEqualTo(interestId);
    }

    @Test
    @DisplayName("구독 정보 제거 성공")
    void removeSubscription_Success() {
        // given
        UUID interestId = UUID.randomUUID();
        UserActivityDocument.SubscriptionInfo subscription = UserActivityDocument.SubscriptionInfo.builder()
                .id(UUID.randomUUID())
                .interestId(interestId)
                .interestName("테스트 관심사")
                .createdAt(Instant.now())
                .build();

        testDocument.getSubscriptions().add(subscription);
        given(userActivityRepository.findByUserId(testUserId)).willReturn(Optional.of(testDocument));
        given(userActivityRepository.save(any(UserActivityDocument.class))).willReturn(testDocument);

        // when
        userActivityService.removeSubscription(testUserId, interestId);

        // then
        verify(userActivityRepository).findByUserId(testUserId);
        verify(userActivityRepository).save(any(UserActivityDocument.class));
        assertThat(testDocument.getSubscriptions()).isEmpty();
    }

    // ========== 댓글 관련 테스트 ==========

    @Test
    @DisplayName("댓글 정보 추가 성공")
    void addComment_Success() {
        // given
        UUID commentId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();
        
        UserActivityDocument.CommentInfo commentInfo = UserActivityDocument.CommentInfo.builder()
                .id(commentId)
                .articleId(articleId)
                .articleTitle("테스트 기사")
                .userId(testUserId)
                .userNickname(testUserNickname)
                .content("테스트 댓글")
                .likeCount(0L)
                .createdAt(Instant.now())
                .build();

        given(userActivityRepository.findByUserId(testUserId)).willReturn(Optional.of(testDocument));
        given(userActivityRepository.save(any(UserActivityDocument.class))).willReturn(testDocument);

        // when
        userActivityService.addComment(testUserId, commentInfo);

        // then
        verify(userActivityRepository).findByUserId(testUserId);
        verify(userActivityRepository).save(any(UserActivityDocument.class));
        assertThat(testDocument.getComments()).hasSize(1);
        assertThat(testDocument.getComments().get(0).getId()).isEqualTo(commentId);
    }

    @Test
    @DisplayName("댓글 정보 업데이트 성공")
    void updateComment_Success() {
        // given
        UUID commentId = UUID.randomUUID();
        String newContent = "수정된 댓글";
        
        UserActivityDocument.CommentInfo existingComment = UserActivityDocument.CommentInfo.builder()
                .id(commentId)
                .content("기존 댓글")
                .build();
        
        testDocument.getComments().add(existingComment);
        given(userActivityRepository.findByUserId(testUserId)).willReturn(Optional.of(testDocument));
        given(userActivityRepository.save(any(UserActivityDocument.class))).willReturn(testDocument);

        // when
        userActivityService.updateComment(testUserId, commentId, newContent);

        // then
        verify(userActivityRepository).findByUserId(testUserId);
        verify(userActivityRepository).save(any(UserActivityDocument.class));
        assertThat(testDocument.getComments().get(0).getContent()).isEqualTo(newContent);
    }

    @Test
    @DisplayName("댓글 정보 제거 성공")
    void removeComment_Success() {
        // given
        UUID commentId = UUID.randomUUID();
        
        UserActivityDocument.CommentInfo existingComment = UserActivityDocument.CommentInfo.builder()
                .id(commentId)
                .content("테스트 댓글")
                .build();
        
        testDocument.getComments().add(existingComment);
        given(userActivityRepository.findByUserId(testUserId)).willReturn(Optional.of(testDocument));
        given(userActivityRepository.save(any(UserActivityDocument.class))).willReturn(testDocument);

        // when
        userActivityService.removeComment(testUserId, commentId);

        // then
        verify(userActivityRepository).findByUserId(testUserId);
        verify(userActivityRepository).save(any(UserActivityDocument.class));
        assertThat(testDocument.getComments()).isEmpty();
    }

    // ========== 좋아요 관련 테스트 ==========

    @Test
    @DisplayName("댓글 좋아요 정보 추가 성공")
    void addCommentLike_Success() {
        // given
        UUID commentId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();
        
        UserActivityDocument.CommentLikeInfo commentLikeInfo = UserActivityDocument.CommentLikeInfo.builder()
                .id(UUID.randomUUID())
                .commentId(commentId)
                .articleId(articleId)
                .articleTitle("테스트 기사")
                .commentUserId(UUID.randomUUID())
                .commentUserNickname("다른사용자")
                .commentContent("좋아요한 댓글")
                .commentLikeCount(1L)
                .createdAt(Instant.now())
                .build();

        given(userActivityRepository.findByUserId(testUserId)).willReturn(Optional.of(testDocument));
        given(userActivityRepository.save(any(UserActivityDocument.class))).willReturn(testDocument);

        // when
        userActivityService.addCommentLike(testUserId, commentLikeInfo);

        // then
        verify(userActivityRepository).findByUserId(testUserId);
        verify(userActivityRepository).save(any(UserActivityDocument.class));
        assertThat(testDocument.getCommentLikes()).hasSize(1);
        assertThat(testDocument.getCommentLikes().get(0).getCommentId()).isEqualTo(commentId);
    }

    @Test
    @DisplayName("댓글 좋아요 정보 제거 성공")
    void removeCommentLike_Success() {
        // given
        UUID commentId = UUID.randomUUID();
        
        UserActivityDocument.CommentLikeInfo existingLike = UserActivityDocument.CommentLikeInfo.builder()
                .commentId(commentId)
                .commentContent("좋아요한 댓글")
                .build();
        
        testDocument.getCommentLikes().add(existingLike);
        given(userActivityRepository.findByUserId(testUserId)).willReturn(Optional.of(testDocument));
        given(userActivityRepository.save(any(UserActivityDocument.class))).willReturn(testDocument);

        // when
        userActivityService.removeCommentLike(testUserId, commentId);

        // then
        verify(userActivityRepository).findByUserId(testUserId);
        verify(userActivityRepository).save(any(UserActivityDocument.class));
        assertThat(testDocument.getCommentLikes()).isEmpty();
    }

    // ========== 기사 조회 관련 테스트 ==========

    @Test
    @DisplayName("기사 조회 정보 추가 성공")
    void addArticleView_Success() {
        // given
        UUID articleId = UUID.randomUUID();
        
        UserActivityDocument.ArticleViewInfo articleViewInfo = UserActivityDocument.ArticleViewInfo.builder()
                .id(UUID.randomUUID())
                .viewedBy(testUserId)
                .articleId(articleId)
                .source("테스트소스")
                .sourceUrl("https://test.com/article/1")
                .articleTitle("테스트 기사")
                .articlePublishedDate(Instant.now())
                .articleSummary("테스트 요약")
                .articleCommentCount(0L)
                .articleViewCount(1L)
                .createdAt(Instant.now())
                .build();

        given(userActivityRepository.findByUserId(testUserId)).willReturn(Optional.of(testDocument));
        given(userActivityRepository.save(any(UserActivityDocument.class))).willReturn(testDocument);

        // when
        userActivityService.addArticleView(testUserId, articleViewInfo);

        // then
        verify(userActivityRepository).findByUserId(testUserId);
        verify(userActivityRepository).save(any(UserActivityDocument.class));
        assertThat(testDocument.getArticleViews()).hasSize(1);
        assertThat(testDocument.getArticleViews().get(0).getArticleId()).isEqualTo(articleId);
    }

    // ========== 전역 업데이트 메서드 테스트 ==========

    @Test
    @DisplayName("좋아요한 댓글 내용 업데이트 성공")
    void updateCommentInLikes_Success() {
        // given
        UUID commentId = UUID.randomUUID();
        String newContent = "수정된 댓글 내용";
        
        UserActivityDocument.CommentLikeInfo commentLike = UserActivityDocument.CommentLikeInfo.builder()
                .commentId(commentId)
                .commentContent("기존 댓글 내용")
                .build();
        
        UserActivityDocument document1 = UserActivityDocument.builder()
                .userId(UUID.randomUUID())
                .commentLikes(new ArrayList<>(Arrays.asList(commentLike)))
                .build();
        
        UserActivityDocument document2 = UserActivityDocument.builder()
                .userId(UUID.randomUUID())
                .commentLikes(new ArrayList<>())
                .build();

        given(userActivityRepository.findAll()).willReturn(Arrays.asList(document1, document2));
        given(userActivityRepository.save(any(UserActivityDocument.class))).willReturn(document1);

        // when
        userActivityService.updateCommentInLikes(commentId, newContent);

        // then
        verify(userActivityRepository).findAll();
        verify(userActivityRepository, times(1)).save(any(UserActivityDocument.class));
        assertThat(document1.getCommentLikes().get(0).getCommentContent()).isEqualTo(newContent);
    }

    @Test
    @DisplayName("관심사 키워드 업데이트 성공")
    void updateInterestKeywords_Success() {
        // given
        UUID interestId = UUID.randomUUID();
        List<String> newKeywords = Arrays.asList("새키워드1", "새키워드2");
        long subscriberCount = 200L;
        
        UserActivityDocument.SubscriptionInfo subscription = UserActivityDocument.SubscriptionInfo.builder()
                .interestId(interestId)
                .interestKeywords(Arrays.asList("기존키워드"))
                .interestSubscriberCount(100L)
                .build();
        
        UserActivityDocument document = UserActivityDocument.builder()
                .userId(UUID.randomUUID())
                .subscriptions(new ArrayList<>(Arrays.asList(subscription)))
                .build();

        given(userActivityRepository.findAll()).willReturn(Arrays.asList(document));
        given(userActivityRepository.save(any(UserActivityDocument.class))).willReturn(document);

        // when
        userActivityService.updateInterestKeywords(interestId, newKeywords, subscriberCount);

        // then
        verify(userActivityRepository).findAll();
        verify(userActivityRepository).save(any(UserActivityDocument.class));
        assertThat(document.getSubscriptions().get(0).getInterestKeywords()).isEqualTo(newKeywords);
        assertThat(document.getSubscriptions().get(0).getInterestSubscriberCount()).isEqualTo(subscriberCount);
    }

    @Test
    @DisplayName("모든 사용자의 구독 목록에서 관심사 제거 성공")
    void removeInterestFromAllSubscriptions_Success() {
        // given
        UUID interestId = UUID.randomUUID();
        
        UserActivityDocument.SubscriptionInfo subscription = UserActivityDocument.SubscriptionInfo.builder()
                .interestId(interestId)
                .interestName("삭제될 관심사")
                .build();
        
        UserActivityDocument document = UserActivityDocument.builder()
                .userId(UUID.randomUUID())
                .subscriptions(new ArrayList<>(Arrays.asList(subscription)))
                .build();

        given(userActivityRepository.findAll()).willReturn(Arrays.asList(document));
        given(userActivityRepository.save(any(UserActivityDocument.class))).willReturn(document);

        // when
        userActivityService.removeInterestFromAllSubscriptions(interestId);

        // then
        verify(userActivityRepository).findAll();
        verify(userActivityRepository).save(any(UserActivityDocument.class));
        assertThat(document.getSubscriptions()).isEmpty();
    }

    // ========== 예외 상황 테스트 ==========

    @Test
    @DisplayName("구독 추가 실패 - 사용자 활동 내역 없음")
    void addSubscription_UserActivityNotFound() {
        // given
        UUID interestId = UUID.randomUUID();
        UserActivityDocument.SubscriptionInfo subscriptionInfo = UserActivityDocument.SubscriptionInfo.builder()
                .interestId(interestId)
                .build();

        given(userActivityRepository.findByUserId(testUserId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userActivityService.addSubscription(testUserId, subscriptionInfo))
                .isInstanceOf(UserActivityNotFoundException.class);

        verify(userActivityRepository).findByUserId(testUserId);
        verify(userActivityRepository, never()).save(any());
    }

    @Test
    @DisplayName("댓글 추가 실패 - 사용자 활동 내역 없음")
    void addComment_UserActivityNotFound() {
        // given
        UserActivityDocument.CommentInfo commentInfo = UserActivityDocument.CommentInfo.builder()
                .id(UUID.randomUUID())
                .build();

        given(userActivityRepository.findByUserId(testUserId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userActivityService.addComment(testUserId, commentInfo))
                .isInstanceOf(UserActivityNotFoundException.class);

        verify(userActivityRepository).findByUserId(testUserId);
        verify(userActivityRepository, never()).save(any());
    }
} 