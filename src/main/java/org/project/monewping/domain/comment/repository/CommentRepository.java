package org.project.monewping.domain.comment.repository;

import org.project.monewping.domain.comment.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 댓글 레포지토리
 * Spring Data JPA를 사용한 기본 CRUD와 커스텀 조회 기능을 제공합니다.
 */
public interface CommentRepository extends JpaRepository<Comment, UUID>, CommentCustomRepository {
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.articleId = :articleId AND c.deleted = false")
    long countByArticleId(@Param("articleId") UUID articleId);
}