package org.project.monewping.domain.comment.repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.project.monewping.domain.comment.domain.Comment;
import org.project.monewping.domain.comment.domain.CommentLike;
import org.project.monewping.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 댓글 좋아요 레포지토리
 * 댓글에 대한 사용자의 좋아요 정보를 조회/저장/삭제합니다.
 */
public interface CommentLikeRepository extends JpaRepository<CommentLike, UUID> {

    // 특정 유저가 특정 댓글에 좋아요를 눌렀는지 조회
    Optional<CommentLike> findByUserAndComment(User user, Comment comment);

    //특정 유저가 특정 댓글에 좋아요를 눌렀는지 여부를 확인
    boolean existsByUserAndComment(User user, Comment comment);

    @Query("SELECT cl.comment.id FROM CommentLike cl WHERE cl.user.id = :userId AND cl.comment.articleId = :articleId")
    Set<UUID> findCommentIdsByUserIdAndArticleId(@Param("userId") UUID userId, @Param("articleId") UUID articleId);

}