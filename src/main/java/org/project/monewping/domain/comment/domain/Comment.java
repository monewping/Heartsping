package org.project.monewping.domain.comment.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 댓글 도메인 엔티티
 * 댓글의 식별자, 내용, 좋아요 수, 작성 시간을 관리한다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String content;
    private Long likeCount;
    private LocalDateTime createdAt;
}