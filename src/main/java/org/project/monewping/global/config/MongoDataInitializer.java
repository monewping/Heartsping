package org.project.monewping.global.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.monewping.domain.useractivity.document.UserActivityDocument;
import org.project.monewping.domain.useractivity.repository.UserActivityRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * MongoDB 더미 데이터 초기화 컴포넌트
 * 
 * <p>
 * 애플리케이션 시작 시 MongoDB에 더미 사용자 활동 데이터를 생성합니다.
 * 개발 환경에서만 실행됩니다.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!test") // 테스트 환경에서는 실행하지 않음
public class MongoDataInitializer implements CommandLineRunner {

    private final UserActivityRepository userActivityRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("MongoDB 더미 데이터 초기화 시작");

        // 기존 데이터가 있으면 초기화하지 않음
        if (userActivityRepository.count() > 0) {
            log.info("MongoDB에 이미 데이터가 존재합니다. 초기화를 건너뜁니다.");
            return;
        }

        try {
            createDummyUserActivities();
            log.info("MongoDB 더미 데이터 초기화 완료");
        } catch (Exception e) {
            log.error("MongoDB 더미 데이터 초기화 중 오류 발생", e);
        }
    }

    private void createDummyUserActivities() {
        List<UserActivityDocument> documents = new ArrayList<>();

        // 사용자 1: 김철수 - IT/기술, 경제/금융 구독
        documents.add(createUserActivity(
                UUID.fromString("550e8400-e29b-41d4-a716-446655440001"),
                "user1@example.com",
                "김철수",
                Instant.now().minusSeconds(86400 * 30), // 30일 전
                List.of(
                        createSubscriptionInfo("IT/기술", List.of("인공지능", "블록체인", "클라우드"), 150),
                        createSubscriptionInfo("경제/금융", List.of("주식", "부동산", "암호화폐"), 200)),
                List.of(
                        createCommentInfo("AI 기술의 혁신적 발전, 새로운 시대 열다", "정말 흥미로운 기사네요! AI 기술의 발전이 정말 놀랍습니다.", 5),
                        createCommentInfo("블록체인 기술, 금융권 혁신 주도", "블록체인 기술이 정말 미래를 바꿀 것 같아요.", 3)),
                List.of(
                        createCommentLikeInfo("주식시장 급등락, 투자자들 주목", "주식시장 상황이 정말 복잡하네요. 신중한 투자가 필요할 것 같습니다.", 3),
                        createCommentLikeInfo("새로운 정책 발표, 국민 반응 엇갈려", "정책에 대한 의견이 분분하네요.", 8)),
                List.of(
                        createArticleViewInfo("AI 기술의 혁신적 발전, 새로운 시대 열다", "chosun"),
                        createArticleViewInfo("주식시장 급등락, 투자자들 주목", "hankyung"),
                        createArticleViewInfo("블록체인 기술, 금융권 혁신 주도", "chosun"))));

        // 사용자 2: 이영희 - 정치/사회, 스포츠 구독
        documents.add(createUserActivity(
                UUID.fromString("550e8400-e29b-41d4-a716-446655440002"),
                "user2@example.com",
                "이영희",
                Instant.now().minusSeconds(86400 * 25), // 25일 전
                List.of(
                        createSubscriptionInfo("정치/사회", List.of("정책", "선거"), 180),
                        createSubscriptionInfo("스포츠", List.of("축구", "야구"), 120)),
                List.of(
                        createCommentInfo("새로운 정책 발표, 국민 반응 엇갈려", "새로운 정책에 대한 의견이 분분하네요. 더 자세한 분석이 필요할 것 같습니다.", 8),
                        createCommentInfo("축구 국가대표팀, 월드컵 예선 승리", "축구 국가대표팀 화이팅! 월드컵에서 좋은 성적 기대합니다.", 12)),
                List.of(
                        createCommentLikeInfo("AI 기술의 혁신적 발전, 새로운 시대 열다", "정말 흥미로운 기사네요! AI 기술의 발전이 정말 놀랍습니다.", 5),
                        createCommentLikeInfo("새로운 영화 개봉, 관객들 호평", "영화가 정말 재미있었다고 하네요. 꼭 봐야겠습니다!", 7)),
                List.of(
                        createArticleViewInfo("새로운 정책 발표, 국민 반응 엇갈려", "chosun"),
                        createArticleViewInfo("축구 국가대표팀, 월드컵 예선 승리", "hankyung"),
                        createArticleViewInfo("대학교육 혁신, 새로운 교육과정 도입", "chosun"))));

        // 사용자 3: 박민수 - 엔터테인먼트, 건강/의료 구독
        documents.add(createUserActivity(
                UUID.fromString("550e8400-e29b-41d4-a716-446655440003"),
                "user3@example.com",
                "박민수",
                Instant.now().minusSeconds(86400 * 20), // 20일 전
                List.of(
                        createSubscriptionInfo("엔터테인먼트", List.of("영화", "음악"), 100),
                        createSubscriptionInfo("건강/의료", List.of("의료기술"), 80)),
                List.of(
                        createCommentInfo("새로운 영화 개봉, 관객들 호평", "영화가 정말 재미있었다고 하네요. 꼭 봐야겠습니다!", 7),
                        createCommentInfo("의료기술 발전, 새로운 치료법 개발", "의료기술 발전이 정말 놀랍네요. 많은 환자들에게 희망이 될 것 같습니다.", 4)),
                List.of(
                        createCommentLikeInfo("축구 국가대표팀, 월드컵 예선 승리", "축구 국가대표팀 화이팅! 월드컵에서 좋은 성적 기대합니다.", 12),
                        createCommentLikeInfo("기후변화 대응, 글로벌 협력 강화", "기후변화 대응이 정말 중요한 문제네요. 모두가 함께 노력해야 할 것 같습니다.", 9)),
                List.of(
                        createArticleViewInfo("새로운 영화 개봉, 관객들 호평", "chosun"),
                        createArticleViewInfo("의료기술 발전, 새로운 치료법 개발", "hankyung"),
                        createArticleViewInfo("기후변화 대응, 글로벌 협력 강화", "hankyung"))));

        // 사용자 4: 최지영 - 교육, 환경 구독
        documents.add(createUserActivity(
                UUID.fromString("550e8400-e29b-41d4-a716-446655440004"),
                "user4@example.com",
                "최지영",
                Instant.now().minusSeconds(86400 * 15), // 15일 전
                List.of(
                        createSubscriptionInfo("교육", List.of("대학교육"), 90),
                        createSubscriptionInfo("환경", List.of("기후변화"), 70)),
                List.of(
                        createCommentInfo("대학교육 혁신, 새로운 교육과정 도입",
                                "대학교육 혁신이 정말 필요했던 것 같아요. 학생들이 더 좋은 교육을 받을 수 있을 것 같습니다.", 6),
                        createCommentInfo("기후변화 대응, 글로벌 협력 강화", "기후변화 대응이 정말 중요한 문제네요. 모두가 함께 노력해야 할 것 같습니다.", 9)),
                List.of(
                        createCommentLikeInfo("새로운 정책 발표, 국민 반응 엇갈려", "새로운 정책에 대한 의견이 분분하네요. 더 자세한 분석이 필요할 것 같습니다.", 8),
                        createCommentLikeInfo("의료기술 발전, 새로운 치료법 개발", "의료기술 발전이 정말 놀랍네요. 많은 환자들에게 희망이 될 것 같습니다.", 4)),
                List.of(
                        createArticleViewInfo("대학교육 혁신, 새로운 교육과정 도입", "chosun"),
                        createArticleViewInfo("기후변화 대응, 글로벌 협력 강화", "hankyung"),
                        createArticleViewInfo("부동산 시장 변화, 투자자들 관심 집중", "hankyung"))));

        // 사용자 5: 정현우 - IT/기술, 스포츠 구독
        documents.add(createUserActivity(
                UUID.fromString("550e8400-e29b-41d4-a716-446655440005"),
                "user5@example.com",
                "정현우",
                Instant.now().minusSeconds(86400 * 10), // 10일 전
                List.of(
                        createSubscriptionInfo("IT/기술", List.of("인공지능", "클라우드"), 150),
                        createSubscriptionInfo("스포츠", List.of("축구", "야구"), 120)),
                List.of(
                        createCommentInfo("AI 기술의 혁신적 발전, 새로운 시대 열다", "AI 기술이 정말 빠르게 발전하고 있네요. 미래가 기대됩니다.", 10),
                        createCommentInfo("축구 국가대표팀, 월드컵 예선 승리", "국가대표팀 화이팅! 좋은 성적 기대합니다.", 15)),
                List.of(
                        createCommentLikeInfo("새로운 영화 개봉, 관객들 호평", "영화가 정말 재미있었다고 하네요. 꼭 봐야겠습니다!", 7),
                        createCommentLikeInfo("블록체인 기술, 금융권 혁신 주도", "블록체인 기술이 정말 미래를 바꿀 것 같아요.", 3)),
                List.of(
                        createArticleViewInfo("AI 기술의 혁신적 발전, 새로운 시대 열다", "chosun"),
                        createArticleViewInfo("축구 국가대표팀, 월드컵 예선 승리", "hankyung"),
                        createArticleViewInfo("블록체인 기술, 금융권 혁신 주도", "chosun"),
                        createArticleViewInfo("부동산 시장 변화, 투자자들 관심 집중", "hankyung"))));

        userActivityRepository.saveAll(documents);
    }

    private UserActivityDocument createUserActivity(
            UUID userId,
            String email,
            String nickname,
            Instant createdAt,
            List<UserActivityDocument.SubscriptionInfo> subscriptions,
            List<UserActivityDocument.CommentInfo> comments,
            List<UserActivityDocument.CommentLikeInfo> commentLikes,
            List<UserActivityDocument.ArticleViewInfo> articleViews) {

        UserActivityDocument.UserInfo userInfo = UserActivityDocument.UserInfo.builder()
                .id(userId)
                .email(email)
                .nickname(nickname)
                .createdAt(createdAt)
                .build();

        return UserActivityDocument.builder()
                .userId(userId)
                .user(userInfo)
                .subscriptions(subscriptions)
                .comments(comments)
                .commentLikes(commentLikes)
                .articleViews(articleViews)
                .updatedAt(Instant.now())
                .build();
    }

    private UserActivityDocument.SubscriptionInfo createSubscriptionInfo(
            String interestName,
            List<String> keywords,
            long subscriberCount) {

        return UserActivityDocument.SubscriptionInfo.builder()
                .id(UUID.randomUUID())
                .interestId(UUID.randomUUID())
                .interestName(interestName)
                .interestKeywords(keywords)
                .interestSubscriberCount(subscriberCount)
                .createdAt(Instant.now().minusSeconds(86400 * 30)) // 30일 전
                .build();
    }

    private UserActivityDocument.CommentInfo createCommentInfo(
            String articleTitle,
            String content,
            long likeCount) {

        return UserActivityDocument.CommentInfo.builder()
                .id(UUID.randomUUID())
                .articleId(UUID.randomUUID())
                .articleTitle(articleTitle)
                .userId(UUID.randomUUID())
                .userNickname("사용자")
                .content(content)
                .likeCount(likeCount)
                .createdAt(Instant.now().minusSeconds(86400 * 7)) // 7일 전
                .build();
    }

    private UserActivityDocument.CommentLikeInfo createCommentLikeInfo(
            String articleTitle,
            String commentContent,
            long commentLikeCount) {

        return UserActivityDocument.CommentLikeInfo.builder()
                .id(UUID.randomUUID())
                .createdAt(Instant.now().minusSeconds(86400 * 3)) // 3일 전
                .commentId(UUID.randomUUID())
                .articleId(UUID.randomUUID())
                .articleTitle(articleTitle)
                .commentUserId(UUID.randomUUID())
                .commentUserNickname("댓글작성자")
                .commentContent(commentContent)
                .commentLikeCount(commentLikeCount)
                .commentCreatedAt(Instant.now().minusSeconds(86400 * 7)) // 7일 전
                .build();
    }

    private UserActivityDocument.ArticleViewInfo createArticleViewInfo(
            String articleTitle,
            String source) {

        return UserActivityDocument.ArticleViewInfo.builder()
                .id(UUID.randomUUID())
                .viewedBy(UUID.randomUUID())
                .createdAt(Instant.now().minusSeconds(86400 * 2)) // 2일 전
                .articleId(UUID.randomUUID())
                .source(source)
                .sourceUrl("https://example.com/article")
                .articleTitle(articleTitle)
                .articlePublishedDate(Instant.now().minusSeconds(86400 * 10)) // 10일 전
                .articleSummary("기사 요약 내용입니다.")
                .articleCommentCount(20L)
                .articleViewCount(1000L)
                .build();
    }
}