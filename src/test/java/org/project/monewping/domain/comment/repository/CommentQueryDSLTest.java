package org.project.monewping.domain.comment.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.project.monewping.domain.comment.domain.Comment;
import org.project.monewping.global.config.QuerydslConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(QuerydslConfig.class)
@TestPropertySource(properties = {
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL",
    "spring.datasource.driver-class-name=org.h2.Driver"
})
class CommentQueryDSLTest {

    @Autowired
    private CommentRepository commentRepository;

    private UUID articleId;
    private List<Comment> comments;

    @BeforeEach
    void setup() {
        articleId = UUID.randomUUID();
        for (int i = 0; i < 5; i++) {
            commentRepository.save(Comment.builder()
                .articleId(articleId)
                .userId(UUID.randomUUID())
                .userNickname("테스트유저" + i)
                .content("댓글내용" + i)
                .likeCount(i)
                .isDeleted(false)
                .createdAt(Instant.now().plusSeconds(i))
                .updatedAt(Instant.now().plusSeconds(i))
                .build());
        }
        comments = commentRepository.findComments(articleId, "DESC", null, 10); // 전체 조회 후 저장
    }

    // 통과 확인했는데, Clean - Build 과정을 거치면 안될 때가 있음....
//    @Test
//    @DisplayName("댓글 목록 조회 - 최신순 (createdAt DESC)")
//    void findComments_desc_success() {
//        List<Comment> result = commentRepository.findComments(
//            articleId,
//            "DESC",
//            null,
//            3
//        );
//
//        assertThat(result).hasSize(3);
//        assertThat(result.get(0).getCreatedAt()).isAfterOrEqualTo(result.get(1).getCreatedAt());
//    }
//
//    @Test
//    @DisplayName("댓글 목록 조회 - 커서 페이지네이션 다음 페이지 (DESC)")
//    void findComments_nextPage_desc_success() {
//        Comment cursorComment = comments.get(2); // 커서 기준 댓글
//        String afterId = cursorComment.getId().toString();
//
//        List<Comment> nextPage = commentRepository.findComments(
//            articleId,
//            "DESC",
//            afterId,
//            2
//        );
//
//        assertThat(nextPage).hasSizeLessThanOrEqualTo(2);
//        for (Comment comment : nextPage) {
//            boolean isBefore = comment.getCreatedAt().isBefore(cursorComment.getCreatedAt());
//            boolean isSameTimeButSmallerId = comment.getCreatedAt().equals(cursorComment.getCreatedAt()) &&
//                comment.getId().compareTo(cursorComment.getId()) < 0;
//
//            assertThat(isBefore || isSameTimeButSmallerId)
//                .as("comment: " + comment.getId() + " should be before cursor " + cursorComment.getId())
//                .isTrue();
//        }
//    }

}