package org.project.monewping.domain.article.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.project.monewping.global.base.BaseEntity;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "news_view_history")
public class NewsViewHistory extends BaseEntity {

    @Column(nullable = false, name = "user_id")
    private UUID userId;

    @Column(nullable = false, name = "article_id")
    private UUID articleId;

    @Column(nullable = false, name = "viewed_at")
    private LocalDateTime viewedAt;

}
