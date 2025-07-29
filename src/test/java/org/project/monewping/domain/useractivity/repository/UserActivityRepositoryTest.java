package org.project.monewping.domain.useractivity.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.project.monewping.domain.useractivity.document.UserActivityDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * UserActivityRepository의 MongoDB 연동 테스트
 * 
 * <p>
 * @DataMongoTest를 사용하여 실제 MongoDB 연산을 테스트합니다.
 * 임베디드 MongoDB를 사용하여 실제 데이터베이스 연산을 검증합니다.
 * </p>
 */
@DataMongoTest
@ActiveProfiles("test")
@DisplayName("UserActivityRepository MongoDB 테스트")
class UserActivityRepositoryTest {

    @Autowired
    private UserActivityRepository userActivityRepository;

    private UUID testUserId1;
    private UUID testUserId2;
    private UserActivityDocument testDocument1;
    private UserActivityDocument testDocument2;
    private Instant testCreatedAt;
    private Instant testUpdatedAt;

    @BeforeEach
    void setUp() {
        // 모든 데이터 삭제
        userActivityRepository.deleteAll();

        testUserId1 = UUID.randomUUID();
        testUserId2 = UUID.randomUUID();
        testCreatedAt = Instant.now().minusSeconds(3600);
        testUpdatedAt = Instant.now();

        // 테스트 데이터 1
        UserActivityDocument.UserInfo userInfo1 = UserActivityDocument.UserInfo.builder()
                .id(testUserId1)
                .email("test1@example.com")
                .nickname("테스트유저1")
                .createdAt(testCreatedAt)
                .build();

        UserActivityDocument.SubscriptionInfo subscription1 = UserActivityDocument.SubscriptionInfo.builder()
                .id(UUID.randomUUID())
                .interestId(UUID.randomUUID())
                .interestName("관심사1")
                .interestKeywords(Arrays.asList("키워드1", "키워드2"))
                .interestSubscriberCount(100L)
                .createdAt(testCreatedAt)
                .build();

        UserActivityDocument.CommentInfo comment1 = UserActivityDocument.CommentInfo.builder()
                .id(UUID.randomUUID())
                .articleId(UUID.randomUUID())
                .articleTitle("기사1")
                .userId(testUserId1)
                .userNickname("테스트유저1")
                .content("댓글1")
                .likeCount(5L)
                .createdAt(testCreatedAt)
                .build();

        testDocument1 = UserActivityDocument.builder()
                .userId(testUserId1)
                .user(userInfo1)
                .subscriptions(new ArrayList<>(Arrays.asList(subscription1)))
                .comments(new ArrayList<>(Arrays.asList(comment1)))
                .commentLikes(new ArrayList<>())
                .articleViews(new ArrayList<>())
                .updatedAt(testUpdatedAt)
                .build();

        // 테스트 데이터 2
        UserActivityDocument.UserInfo userInfo2 = UserActivityDocument.UserInfo.builder()
                .id(testUserId2)
                .email("test2@example.com")
                .nickname("테스트유저2")
                .createdAt(testCreatedAt)
                .build();

        testDocument2 = UserActivityDocument.builder()
                .userId(testUserId2)
                .user(userInfo2)
                .subscriptions(new ArrayList<>())
                .comments(new ArrayList<>())
                .commentLikes(new ArrayList<>())
                .articleViews(new ArrayList<>())
                .updatedAt(testUpdatedAt)
                .build();
    }

    @Test
    @DisplayName("UserActivityDocument 저장 성공")
    void save_Success() {
        // when
        UserActivityDocument saved = userActivityRepository.save(testDocument1);

        // then
        assertThat(saved).isNotNull();
        assertThat(saved.getUserId()).isEqualTo(testUserId1);
        assertThat(saved.getUser().getEmail()).isEqualTo("test1@example.com");
        assertThat(saved.getUser().getNickname()).isEqualTo("테스트유저1");
        assertThat(saved.getSubscriptions()).hasSize(1);
        assertThat(saved.getComments()).hasSize(1);
        assertThat(saved.getCommentLikes()).isEmpty();
        assertThat(saved.getArticleViews()).isEmpty();
    }

    @Test
    @DisplayName("userId로 UserActivityDocument 조회 성공")
    void findByUserId_Success() {
        // given
        userActivityRepository.save(testDocument1);
        userActivityRepository.save(testDocument2);

        // when
        Optional<UserActivityDocument> result = userActivityRepository.findByUserId(testUserId1);

        // then
        assertThat(result).isPresent();
        UserActivityDocument found = result.get();
        assertThat(found.getUserId()).isEqualTo(testUserId1);
        assertThat(found.getUser().getEmail()).isEqualTo("test1@example.com");
        assertThat(found.getSubscriptions()).hasSize(1);
        assertThat(found.getComments()).hasSize(1);
    }

    @Test
    @DisplayName("존재하지 않는 userId로 조회 시 빈 결과 반환")
    void findByUserId_NotFound() {
        // given
        userActivityRepository.save(testDocument1);
        UUID nonExistentUserId = UUID.randomUUID();

        // when
        Optional<UserActivityDocument> result = userActivityRepository.findByUserId(nonExistentUserId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("userId 존재 여부 확인 성공")
    void existsByUserId_Success() {
        // given
        userActivityRepository.save(testDocument1);

        // when & then
        assertThat(userActivityRepository.existsByUserId(testUserId1)).isTrue();
        assertThat(userActivityRepository.existsByUserId(testUserId2)).isFalse();
    }

    @Test
    @DisplayName("userId로 UserActivityDocument 삭제 성공")
    void deleteByUserId_Success() {
        // given
        userActivityRepository.save(testDocument1);
        userActivityRepository.save(testDocument2);
        
        // 저장 확인
        assertThat(userActivityRepository.existsByUserId(testUserId1)).isTrue();
        assertThat(userActivityRepository.existsByUserId(testUserId2)).isTrue();

        // when
        userActivityRepository.deleteByUserId(testUserId1);

        // then
        assertThat(userActivityRepository.existsByUserId(testUserId1)).isFalse();
        assertThat(userActivityRepository.existsByUserId(testUserId2)).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 userId 삭제 시 오류 없이 실행")
    void deleteByUserId_NotFound() {
        // given
        UUID nonExistentUserId = UUID.randomUUID();

        // when & then (예외 발생하지 않음)
        userActivityRepository.deleteByUserId(nonExistentUserId);
        
        // 다른 데이터에 영향 없음 확인
        userActivityRepository.save(testDocument1);
        assertThat(userActivityRepository.existsByUserId(testUserId1)).isTrue();
    }

    @Test
    @DisplayName("모든 UserActivityDocument 조회 성공")
    void findAll_Success() {
        // given
        userActivityRepository.save(testDocument1);
        userActivityRepository.save(testDocument2);

        // when
        List<UserActivityDocument> results = userActivityRepository.findAll();

        // then
        assertThat(results).hasSize(2);
        assertThat(results)
                .extracting(UserActivityDocument::getUserId)
                .containsExactlyInAnyOrder(testUserId1, testUserId2);
    }

    @Test
    @DisplayName("빈 컬렉션으로 findAll 호출 시 빈 리스트 반환")
    void findAll_Empty() {
        // when
        List<UserActivityDocument> results = userActivityRepository.findAll();

        // then
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("UserActivityDocument 업데이트 성공")
    void update_Success() {
        // given
        UserActivityDocument saved = userActivityRepository.save(testDocument1);
        
        // 새로운 구독 추가
        UserActivityDocument.SubscriptionInfo newSubscription = UserActivityDocument.SubscriptionInfo.builder()
                .id(UUID.randomUUID())
                .interestId(UUID.randomUUID())
                .interestName("새로운 관심사")
                .interestKeywords(Arrays.asList("새키워드"))
                .interestSubscriberCount(50L)
                .createdAt(Instant.now())
                .build();

        saved.getSubscriptions().add(newSubscription);
        saved.setUpdatedAt(Instant.now());

        // when
        UserActivityDocument updated = userActivityRepository.save(saved);

        // then
        assertThat(updated.getSubscriptions()).hasSize(2);
        assertThat(updated.getSubscriptions())
                .extracting(UserActivityDocument.SubscriptionInfo::getInterestName)
                .containsExactlyInAnyOrder("관심사1", "새로운 관심사");
    }

    @Test
    @DisplayName("복잡한 데이터 구조 저장 및 조회 성공")
    void complexDataStructure_Success() {
        // given - 모든 타입의 활동이 포함된 복잡한 문서
        UserActivityDocument.UserInfo userInfo = UserActivityDocument.UserInfo.builder()
                .id(testUserId1)
                .email("complex@example.com")
                .nickname("복잡한유저")
                .createdAt(testCreatedAt)
                .build();

        List<UserActivityDocument.SubscriptionInfo> subscriptions = Arrays.asList(
                UserActivityDocument.SubscriptionInfo.builder()
                        .id(UUID.randomUUID())
                        .interestId(UUID.randomUUID())
                        .interestName("관심사A")
                        .interestKeywords(Arrays.asList("키워드A1", "키워드A2"))
                        .interestSubscriberCount(200L)
                        .createdAt(testCreatedAt)
                        .build(),
                UserActivityDocument.SubscriptionInfo.builder()
                        .id(UUID.randomUUID())
                        .interestId(UUID.randomUUID())
                        .interestName("관심사B")
                        .interestKeywords(Arrays.asList("키워드B1"))
                        .interestSubscriberCount(150L)
                        .createdAt(testCreatedAt)
                        .build()
        );

        List<UserActivityDocument.CommentInfo> comments = Arrays.asList(
                UserActivityDocument.CommentInfo.builder()
                        .id(UUID.randomUUID())
                        .articleId(UUID.randomUUID())
                        .articleTitle("기사A")
                        .userId(testUserId1)
                        .userNickname("복잡한유저")
                        .content("댓글A")
                        .likeCount(10L)
                        .createdAt(testCreatedAt)
                        .build(),
                UserActivityDocument.CommentInfo.builder()
                        .id(UUID.randomUUID())
                        .articleId(UUID.randomUUID())
                        .articleTitle("기사B")
                        .userId(testUserId1)
                        .userNickname("복잡한유저")
                        .content("댓글B")
                        .likeCount(5L)
                        .createdAt(testCreatedAt)
                        .build()
        );

        List<UserActivityDocument.CommentLikeInfo> commentLikes = Arrays.asList(
                UserActivityDocument.CommentLikeInfo.builder()
                        .id(UUID.randomUUID())
                        .commentId(UUID.randomUUID())
                        .articleId(UUID.randomUUID())
                        .articleTitle("좋아요한 기사A")
                        .commentUserId(UUID.randomUUID())
                        .commentUserNickname("다른유저A")
                        .commentContent("좋아요한 댓글A")
                        .commentLikeCount(3L)
                        .commentCreatedAt(testCreatedAt)
                        .createdAt(testCreatedAt)
                        .build()
        );

        List<UserActivityDocument.ArticleViewInfo> articleViews = Arrays.asList(
                UserActivityDocument.ArticleViewInfo.builder()
                        .id(UUID.randomUUID())
                        .viewedBy(testUserId1)
                        .articleId(UUID.randomUUID())
                        .source("테스트소스")
                        .sourceUrl("https://test.com/article/1")
                        .articleTitle("조회한 기사A")
                        .articlePublishedDate(testCreatedAt)
                        .articleSummary("기사 요약A")
                        .articleCommentCount(20L)
                        .articleViewCount(100L)
                        .createdAt(testCreatedAt)
                        .build()
        );

        UserActivityDocument complexDocument = UserActivityDocument.builder()
                .userId(testUserId1)
                .user(userInfo)
                .subscriptions(subscriptions)
                .comments(comments)
                .commentLikes(commentLikes)
                .articleViews(articleViews)
                .updatedAt(testUpdatedAt)
                .build();

        // when
        UserActivityDocument saved = userActivityRepository.save(complexDocument);
        Optional<UserActivityDocument> found = userActivityRepository.findByUserId(testUserId1);

        // then
        assertThat(found).isPresent();
        UserActivityDocument result = found.get();
        
        assertThat(result.getUserId()).isEqualTo(testUserId1);
        assertThat(result.getUser().getNickname()).isEqualTo("복잡한유저");
        assertThat(result.getSubscriptions()).hasSize(2);
        assertThat(result.getComments()).hasSize(2);
        assertThat(result.getCommentLikes()).hasSize(1);
        assertThat(result.getArticleViews()).hasSize(1);

        // 세부 데이터 검증
        assertThat(result.getSubscriptions())
                .extracting(UserActivityDocument.SubscriptionInfo::getInterestName)
                .containsExactlyInAnyOrder("관심사A", "관심사B");

        assertThat(result.getComments())
                .extracting(UserActivityDocument.CommentInfo::getContent)
                .containsExactlyInAnyOrder("댓글A", "댓글B");

        assertThat(result.getCommentLikes().get(0).getCommentContent()).isEqualTo("좋아요한 댓글A");
        assertThat(result.getArticleViews().get(0).getArticleTitle()).isEqualTo("조회한 기사A");
    }

    @Test
    @DisplayName("null 필드가 있는 문서 저장 및 조회 성공")
    void saveWithNullFields_Success() {
        // given
        UserActivityDocument.UserInfo userInfo = UserActivityDocument.UserInfo.builder()
                .id(testUserId1)
                .email("null-test@example.com")
                .nickname("널테스트유저")
                .createdAt(testCreatedAt)
                .build();

        UserActivityDocument documentWithNulls = UserActivityDocument.builder()
                .userId(testUserId1)
                .user(userInfo)
                .subscriptions(null) // null 허용
                .comments(null)
                .commentLikes(null)
                .articleViews(null)
                .updatedAt(testUpdatedAt)
                .build();

        // when
        UserActivityDocument saved = userActivityRepository.save(documentWithNulls);
        Optional<UserActivityDocument> found = userActivityRepository.findByUserId(testUserId1);

        // then
        assertThat(found).isPresent();
        UserActivityDocument result = found.get();
        assertThat(result.getUserId()).isEqualTo(testUserId1);
        assertThat(result.getUser().getNickname()).isEqualTo("널테스트유저");
        // null 필드들은 MongoDB에서 어떻게 처리되는지에 따라 null 또는 빈 컬렉션일 수 있음
    }

    @Test
    @DisplayName("대용량 데이터 저장 및 조회 성공")
    void largeDataSet_Success() {
        // given - 각 컬렉션에 여러 항목이 있는 문서
        UserActivityDocument.UserInfo userInfo = UserActivityDocument.UserInfo.builder()
                .id(testUserId1)
                .email("large@example.com")
                .nickname("대용량유저")
                .createdAt(testCreatedAt)
                .build();

        List<UserActivityDocument.SubscriptionInfo> manySubscriptions = new ArrayList<>();
        List<UserActivityDocument.CommentInfo> manyComments = new ArrayList<>();
        
        // 10개의 구독과 댓글 생성
        for (int i = 0; i < 10; i++) {
            manySubscriptions.add(UserActivityDocument.SubscriptionInfo.builder()
                    .id(UUID.randomUUID())
                    .interestId(UUID.randomUUID())
                    .interestName("관심사" + i)
                    .interestKeywords(Arrays.asList("키워드" + i + "A", "키워드" + i + "B"))
                    .interestSubscriberCount((long) (i * 10))
                    .createdAt(testCreatedAt.plusSeconds(i))
                    .build());

            manyComments.add(UserActivityDocument.CommentInfo.builder()
                    .id(UUID.randomUUID())
                    .articleId(UUID.randomUUID())
                    .articleTitle("기사" + i)
                    .userId(testUserId1)
                    .userNickname("대용량유저")
                    .content("댓글내용" + i)
                    .likeCount((long) i)
                    .createdAt(testCreatedAt.plusSeconds(i))
                    .build());
        }

        UserActivityDocument largeDocument = UserActivityDocument.builder()
                .userId(testUserId1)
                .user(userInfo)
                .subscriptions(manySubscriptions)
                .comments(manyComments)
                .commentLikes(new ArrayList<>())
                .articleViews(new ArrayList<>())
                .updatedAt(testUpdatedAt)
                .build();

        // when
        UserActivityDocument saved = userActivityRepository.save(largeDocument);
        Optional<UserActivityDocument> found = userActivityRepository.findByUserId(testUserId1);

        // then
        assertThat(found).isPresent();
        UserActivityDocument result = found.get();
        assertThat(result.getSubscriptions()).hasSize(10);
        assertThat(result.getComments()).hasSize(10);
        
        // 순서와 내용 확인
        assertThat(result.getSubscriptions())
                .extracting(UserActivityDocument.SubscriptionInfo::getInterestName)
                .contains("관심사0", "관심사5", "관심사9");
                
        assertThat(result.getComments())
                .extracting(UserActivityDocument.CommentInfo::getContent)
                .contains("댓글내용0", "댓글내용5", "댓글내용9");
    }
} 