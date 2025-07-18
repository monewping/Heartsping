package org.project.monewping.domain.comment.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
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
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Test
    @DisplayName("articleId로 댓글 개수 조회 성공")
    void countByArticleId_success() {
        UUID articleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Comment comment = Comment.builder()
            .articleId(articleId)
            .userId(userId)
            .userNickname("테스트유저")
            .content("테스트 댓글")
            .likeCount(0)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .isDeleted(false)
            .build();

        commentRepository.save(comment);

        long count = commentRepository.countByArticleId(articleId);
        assertThat(count).isEqualTo(1L);
    }
}