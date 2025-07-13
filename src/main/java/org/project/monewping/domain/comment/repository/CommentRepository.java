package org.project.monewping.domain.comment.repository;

import org.project.monewping.domain.comment.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 댓글 도메인 레포지토리
 * JPA를 사용하여 댓글 데이터를 조회/저장한다.
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    /**
     * ID 기준 내림차순으로 커서 기반 조회 (최대 10개)
     *
     * @param cursorId 조회 시작 기준 ID
     * @return 댓글 목록
     */
    List<Comment> findTop10ByIdLessThanOrderByIdDesc(Long cursorId);
}