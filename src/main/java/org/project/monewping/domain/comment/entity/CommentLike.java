package org.project.monewping.domain.comment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.project.monewping.domain.user.entity.User;
import org.project.monewping.global.base.BaseEntity;

/**
 * 댓글 좋아요 도메인 엔티티
 * 댓글에 대한 사용자의 좋아요 정보를 관리합니다.
 * 하나의 유저는 하나의 댓글에 한 번만 좋아요를 누를 수 있습니다.
 *
 * - liked_id: 좋아요를 누른 사용자 식별자
 * - comment_id: 좋아요 대상 댓글 식별자
 * - created_at: 좋아요 생성 시간 (BaseEntity)
 */
@Entity
@Table(
    name = "comment_likes",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"liked_id", "comment_id"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CommentLike extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "liked_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "comment_id", nullable = false)
  private Comment comment;
}
