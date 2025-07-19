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
    }

    @Test
    @DisplayName("댓글 목록 조회 - 최신순 DESC")
    void findComments_desc_success() {
        List<Comment> comments = commentRepository.findComments(
            articleId,
            "createdAt",
            "DESC",
            null,
            null,
            null,
            5
        );

        assertThat(comments).hasSize(5);
        assertThat(comments.get(0).getCreatedAt()).isAfterOrEqualTo(comments.get(1).getCreatedAt());
        assertThat(comments.get(1).getCreatedAt()).isAfterOrEqualTo(comments.get(2).getCreatedAt());
    }

    @Test
    @DisplayName("댓글 목록 조회 - 오래된순 ASC")
    void findComments_asc_success() {
        List<Comment> comments = commentRepository.findComments(
            articleId,
            "createdAt",
            "ASC",
            null,
            null,
            null,
            5
        );

        assertThat(comments).hasSize(5);
        assertThat(comments.get(0).getCreatedAt()).isBeforeOrEqualTo(comments.get(1).getCreatedAt());
        assertThat(comments.get(1).getCreatedAt()).isBeforeOrEqualTo(comments.get(2).getCreatedAt());
    }

    @Test
    @DisplayName("댓글 목록 조회 - 좋아요 ASC")
    void findComments_orderByLikeCount_asc_success() {
        List<Comment> comments = commentRepository.findComments(
            articleId,
            "likeCount",
            "ASC",
            null,
            null,
            null,
            5
        );

        assertThat(comments).hasSize(5);
        assertThat(comments.get(0).getLikeCount()).isLessThanOrEqualTo(comments.get(1).getLikeCount());
        assertThat(comments.get(1).getLikeCount()).isLessThanOrEqualTo(comments.get(2).getLikeCount());
    }
}