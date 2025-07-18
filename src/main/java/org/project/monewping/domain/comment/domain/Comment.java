package org.project.monewping.domain.comment.domain;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.project.monewping.global.base.BaseUpdatableEntity;

/**
 * 댓글 도메인 엔티티
 * 댓글의 식별자, 내용, 좋아요 수, 작성 시간을 관리합니다.
 */
@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "comments")
public class Comment extends BaseUpdatableEntity {

    @Column(name = "article_id", nullable = false)
    private UUID articleId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "user_nickname", nullable = false)
    private String userNickname;

    @Column(nullable = false)
    private String content;

    @Column(name = "like_count", nullable = false)
    private int likeCount;

    @Column(nullable = false)
    private Boolean deleted;
}