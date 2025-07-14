package org.project.monewping.domain.comment.domain;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 댓글 도메인 엔티티
 * 댓글의 식별자, 내용, 좋아요 수, 작성 시간을 관리합니다.
 */
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private UUID articleId;
    private String content;
    private String nickname;
    private int likeCount;
    private LocalDateTime createdAt;
}