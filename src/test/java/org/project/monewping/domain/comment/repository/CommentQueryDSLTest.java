package org.project.monewping.domain.comment.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.project.monewping.domain.comment.domain.Comment;
import org.project.monewping.global.config.QuerydslConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;


@Import(QuerydslConfig.class)
@DataJpaTest
class CommentQueryDSLTest {

    @Autowired
    private CommentRepository commentRepository;

    @Test
    @DisplayName("findComments - 기본 createdAt DESC 정상 조회")
    void findComments_success() {
        UUID articleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Comment comment1 = Comment.builder()
            .articleId(articleId)
            .userId(userId)
            .userNickname("사용자1")
            .content("첫 번째 댓글")
            .likeCount(0)
            .createdAt(Instant.now().minusSeconds(100))
            .updatedAt(Instant.now().minusSeconds(100))
            .isDeleted(false)
            .build();

        Comment comment2 = Comment.builder()
            .articleId(articleId)
            .userId(userId)
            .userNickname("사용자2")
            .content("두 번째 댓글")
            .likeCount(0)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .isDeleted(false)
            .build();

        commentRepository.save(comment1);
        commentRepository.save(comment2);

        List<Comment> results = commentRepository.findComments(
            articleId,
            "createdAt",
            "DESC",
            null,
            null,
            10
        );

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getContent()).isEqualTo("두 번째 댓글");
        assertThat(results.get(1).getContent()).isEqualTo("첫 번째 댓글");
    }
}
