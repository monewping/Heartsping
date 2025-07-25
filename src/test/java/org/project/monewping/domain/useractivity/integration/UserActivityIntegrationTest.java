package org.project.monewping.domain.useractivity.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.project.monewping.domain.useractivity.document.UserActivityDocument;
import org.project.monewping.domain.useractivity.dto.UserActivityDto;
import org.project.monewping.domain.useractivity.exception.UserActivityNotFoundException;
import org.project.monewping.domain.useractivity.repository.UserActivityRepository;
import org.project.monewping.domain.useractivity.service.UserActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * UserActivity 도메인 MongoDB 통합 테스트
 * 
 * <p>
 * 임베디드 MongoDB를 사용하여 실제 MongoDB와 연동된 통합 테스트를 수행합니다.
 * </p>
 */
@DataMongoTest
@ActiveProfiles("test")
@Import({
    org.project.monewping.domain.useractivity.service.UserActivityServiceImpl.class,
    org.project.monewping.domain.useractivity.mapper.UserActivityMapperImpl.class
})
@DisplayName("UserActivity MongoDB 통합 테스트")
class UserActivityIntegrationTest {

    @Autowired
    private UserActivityRepository userActivityRepository;

    @Autowired
    private UserActivityService userActivityService;

    private UUID testUserId;
    private Instant testCreatedAt;

    @BeforeEach
    void setUp() {
        // MongoDB 데이터 초기화
        userActivityRepository.deleteAll();
        
        testUserId = UUID.randomUUID();
        testCreatedAt = Instant.now().minusSeconds(3600);
    }

    @Test
    @DisplayName("사용자 활동 초기화 및 조회 통합 테스트")
    void initializeAndGetUserActivity_IntegrationTest() {
        // given
        String email = "integration@example.com";
        String nickname = "통합테스트유저";

        // when - 사용자 활동 초기화
        userActivityService.initializeUserActivity(testUserId, email, nickname, testCreatedAt);

        // then - 초기화된 사용자 활동 조회
        UserActivityDto result = userActivityService.getUserActivity(testUserId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testUserId);
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getNickname()).isEqualTo(nickname);
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getSubscriptions()).isEmpty();
        assertThat(result.getComments()).isEmpty();
        assertThat(result.getCommentLikes()).isEmpty();
        assertThat(result.getArticleViews()).isEmpty();
        assertThat(result.getUpdatedAt()).isNotNull();

        // MongoDB에서 직접 확인
        Optional<UserActivityDocument> document = userActivityRepository.findByUserId(testUserId);
        assertThat(document).isPresent();
        assertThat(document.get().getUser().getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("구독 추가 및 조회 MongoDB 통합 테스트")
    void addSubscriptionAndGet_MongoIntegrationTest() {
        // given - 사용자 활동 초기화
        userActivityService.initializeUserActivity(testUserId, "sub@example.com", "구독유저", testCreatedAt);

        UUID subscriptionId = UUID.randomUUID();
        UUID interestId = UUID.randomUUID();
        String interestName = "MongoDB통합테스트관심사";

        UserActivityDocument.SubscriptionInfo subscriptionInfo = UserActivityDocument.SubscriptionInfo.builder()
                .id(subscriptionId)
                .interestId(interestId)
                .interestName(interestName)
                .interestKeywords(Arrays.asList("몽고DB", "통합테스트"))
                .interestSubscriberCount(100L)
                .createdAt(testCreatedAt)
                .build();

        // when - 구독 추가
        userActivityService.addSubscription(testUserId, subscriptionInfo);

        // then - MongoDB에서 직접 확인
        Optional<UserActivityDocument> document = userActivityRepository.findByUserId(testUserId);
        assertThat(document).isPresent();
        assertThat(document.get().getSubscriptions()).hasSize(1);
        assertThat(document.get().getSubscriptions().get(0).getInterestName()).isEqualTo(interestName);
        assertThat(document.get().getSubscriptions().get(0).getInterestKeywords()).containsExactly("몽고DB", "통합테스트");

        // 서비스를 통한 조회도 정상 동작
        UserActivityDto result = userActivityService.getUserActivity(testUserId);
        assertThat(result.getSubscriptions()).hasSize(1);
        assertThat(result.getSubscriptions().get(0).getInterestName()).isEqualTo(interestName);
    }

    @Test
    @DisplayName("댓글 추가 및 MongoDB 영속성 테스트")
    void addCommentAndPersistence_MongoIntegrationTest() {
        // given - 사용자 활동 초기화
        userActivityService.initializeUserActivity(testUserId, "comment@example.com", "댓글유저", testCreatedAt);

        UUID commentId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();
        String articleTitle = "MongoDB댓글테스트기사";
        String content = "MongoDB에 저장되는 댓글";

        UserActivityDocument.CommentInfo commentInfo = UserActivityDocument.CommentInfo.builder()
                .id(commentId)
                .articleId(articleId)
                .articleTitle(articleTitle)
                .userId(testUserId)
                .userNickname("댓글유저")
                .content(content)
                .likeCount(5L)
                .createdAt(testCreatedAt)
                .build();

        // when - 댓글 추가
        userActivityService.addComment(testUserId, commentInfo);

        // then - MongoDB 직접 조회로 영속성 확인
        Optional<UserActivityDocument> document = userActivityRepository.findByUserId(testUserId);
        assertThat(document).isPresent();
        
        UserActivityDocument doc = document.get();
        assertThat(doc.getComments()).hasSize(1);
        assertThat(doc.getComments().get(0).getContent()).isEqualTo(content);
        assertThat(doc.getComments().get(0).getArticleTitle()).isEqualTo(articleTitle);
        assertThat(doc.getComments().get(0).getLikeCount()).isEqualTo(5L);

        // 업데이트 시간이 설정되었는지 확인
        assertThat(doc.getUpdatedAt()).isAfter(testCreatedAt);
    }

    @Test
    @DisplayName("사용자 활동 삭제 MongoDB 통합 테스트")
    void deleteUserActivity_MongoIntegrationTest() {
        // given - 사용자 활동 초기화 및 데이터 추가
        userActivityService.initializeUserActivity(testUserId, "delete@example.com", "삭제유저", testCreatedAt);
        
        // 구독 추가
        UserActivityDocument.SubscriptionInfo subscriptionInfo = UserActivityDocument.SubscriptionInfo.builder()
                .id(UUID.randomUUID())
                .interestId(UUID.randomUUID())
                .interestName("삭제될관심사")
                .interestKeywords(Arrays.asList("삭제테스트"))
                .interestSubscriberCount(50L)
                .createdAt(testCreatedAt)
                .build();
        userActivityService.addSubscription(testUserId, subscriptionInfo);

        // 데이터가 존재하는지 확인
        assertThat(userActivityRepository.existsByUserId(testUserId)).isTrue();

        // when - 사용자 활동 삭제
        userActivityService.deleteUserActivity(testUserId);

        // then - MongoDB에서 완전히 삭제되었는지 확인
        assertThat(userActivityRepository.existsByUserId(testUserId)).isFalse();
        assertThat(userActivityRepository.findByUserId(testUserId)).isEmpty();
        
        // 삭제된 사용자 활동 조회 시 예외 발생
        assertThatThrownBy(() -> userActivityService.getUserActivity(testUserId))
                .isInstanceOf(UserActivityNotFoundException.class);
    }

    @Test
    @DisplayName("복합 데이터 저장 및 MongoDB 조회 성능 테스트")
    void complexDataStorageAndQuery_MongoIntegrationTest() {
        // given - 사용자 활동 초기화
        userActivityService.initializeUserActivity(testUserId, "complex@mongo.com", "복합데이터유저", testCreatedAt);

        // when - 여러 종류의 활동 데이터 추가
        // 1. 구독 3개 추가
        for (int i = 1; i <= 3; i++) {
            UserActivityDocument.SubscriptionInfo subscription = UserActivityDocument.SubscriptionInfo.builder()
                    .id(UUID.randomUUID())
                    .interestId(UUID.randomUUID())
                    .interestName("MongoDB관심사" + i)
                    .interestKeywords(Arrays.asList("키워드" + i + "A", "키워드" + i + "B"))
                    .interestSubscriberCount((long) i * 100)
                    .createdAt(testCreatedAt)
                    .build();
            userActivityService.addSubscription(testUserId, subscription);
        }

        // 2. 댓글 2개 추가
        for (int i = 1; i <= 2; i++) {
            UserActivityDocument.CommentInfo comment = UserActivityDocument.CommentInfo.builder()
                    .id(UUID.randomUUID())
                    .articleId(UUID.randomUUID())
                    .articleTitle("MongoDB기사" + i)
                    .userId(testUserId)
                    .userNickname("복합데이터유저")
                    .content("MongoDB에 저장된 댓글" + i)
                    .likeCount((long) i * 10)
                    .createdAt(testCreatedAt)
                    .build();
            userActivityService.addComment(testUserId, comment);
        }

        // then - MongoDB에서 복합 데이터 조회 및 검증
        Optional<UserActivityDocument> documentOpt = userActivityRepository.findByUserId(testUserId);
        assertThat(documentOpt).isPresent();
        
        UserActivityDocument document = documentOpt.get();
        
        // 구독 데이터 검증
        assertThat(document.getSubscriptions()).hasSize(3);
        assertThat(document.getSubscriptions())
                .extracting(UserActivityDocument.SubscriptionInfo::getInterestName)
                .containsExactlyInAnyOrder("MongoDB관심사1", "MongoDB관심사2", "MongoDB관심사3");

        // 댓글 데이터 검증
        assertThat(document.getComments()).hasSize(2);
        assertThat(document.getComments())
                .extracting(UserActivityDocument.CommentInfo::getContent)
                .containsExactlyInAnyOrder("MongoDB에 저장된 댓글1", "MongoDB에 저장된 댓글2");

        // 서비스 레이어를 통한 조회도 정상 동작
        UserActivityDto dto = userActivityService.getUserActivity(testUserId);
        assertThat(dto.getSubscriptions()).hasSize(3);
        assertThat(dto.getComments()).hasSize(2);
        assertThat(dto.getCommentLikes()).isEmpty();
        assertThat(dto.getArticleViews()).isEmpty();
    }

    @Test
    @DisplayName("MongoDB 트랜잭션 및 일관성 테스트")
    void mongoTransactionConsistency_IntegrationTest() {
        // given
        String email = "transaction@mongo.com";
        String nickname = "트랜잭션유저";

        // when - 사용자 활동 초기화
        userActivityService.initializeUserActivity(testUserId, email, nickname, testCreatedAt);

        // then - 초기 상태 확인
        Optional<UserActivityDocument> initialDoc = userActivityRepository.findByUserId(testUserId);
        assertThat(initialDoc).isPresent();
        Instant initialUpdatedAt = initialDoc.get().getUpdatedAt();

        // when - 구독 추가 (업데이트 발생)
        UserActivityDocument.SubscriptionInfo newSubscription = UserActivityDocument.SubscriptionInfo.builder()
                .id(UUID.randomUUID())
                .interestId(UUID.randomUUID())
                .interestName("트랜잭션테스트관심사")
                .interestKeywords(Arrays.asList("트랜잭션", "일관성"))
                .interestSubscriberCount(1L)
                .createdAt(testCreatedAt)
                .build();
        
        userActivityService.addSubscription(testUserId, newSubscription);

        // then - 업데이트 후 일관성 확인
        Optional<UserActivityDocument> updatedDoc = userActivityRepository.findByUserId(testUserId);
        assertThat(updatedDoc).isPresent();
        
        UserActivityDocument doc = updatedDoc.get();
        assertThat(doc.getUpdatedAt()).isAfter(initialUpdatedAt);
        assertThat(doc.getSubscriptions()).hasSize(1);
        assertThat(doc.getUser().getEmail()).isEqualTo(email); // 기존 데이터 유지
        assertThat(doc.getUser().getNickname()).isEqualTo(nickname); // 기존 데이터 유지
    }
} 