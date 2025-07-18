package org.project.monewping.domain.article.entity;


import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.project.monewping.domain.interest.entity.Interest;
import org.project.monewping.global.base.BaseEntity;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table
public class Articles extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interest_id", nullable = false)
    private Interest interest;

    @Column(nullable = false, length = 10)
    private String source;

    @Column(name = "original_link", nullable = false, length = 300, unique = true)
    private String originalLink;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String summary;

    @Column(name = "published_at",  nullable = false)
    private LocalDateTime publishedAt;

    @Column(name = "comment_count", nullable = false)
    private long commentCount;

    @Column(name = "view_count", nullable = false)
    private long viewCount;

    @Column(nullable = false, name = "is_deleted")
    private boolean deleted;

    // 논리 삭제 시 적용될 마스킹
    public void softDeleteWithMasking() {
        this.title = "[ 삭제된 기사 ]";
        this.summary = "해당 기사는 삭제되었습니다.";
        this.originalLink = "404 Not Found - " + this.getId();
        this.deleted = true;
    }

}
