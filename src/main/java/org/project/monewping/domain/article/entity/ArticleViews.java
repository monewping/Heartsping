package org.project.monewping.domain.article.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "article_views")
public class ArticleViews {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(nullable = false, name = "user_id")
    private UUID viewedBy;

    @Column(nullable = false, name = "article_id")
    private UUID articleId;

    @Column(nullable = false, name = "viewed_at")
    private LocalDateTime articlePublishedDate;

}
