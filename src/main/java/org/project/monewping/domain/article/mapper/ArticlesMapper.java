package org.project.monewping.domain.article.mapper;

import java.time.LocalDateTime;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.project.monewping.domain.article.dto.data.ArticleDto;
import org.project.monewping.domain.article.dto.request.ArticleSaveRequest;
import org.project.monewping.domain.article.entity.Articles;
import org.project.monewping.domain.interest.entity.Interest;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface ArticlesMapper {

    @Mapping(target = "interest", source = "interest")
    @Mapping(target = "viewCount", constant = "0L")
    @Mapping(target = "commentCount", constant = "0L")
    @Mapping(target = "deleted", constant = "false")
    Articles toEntity(ArticleSaveRequest request, Interest interest);

    @Mapping(source = "originalLink", target = "sourceUrl")
    @Mapping(source = "publishedAt", target = "publishDate")
    @Mapping(target = "viewedByMe", ignore = true)
    @Mapping(target = "withViewedByMe", ignore = true)
    ArticleDto toDto(Articles article);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "publishedAt", source = "publishDate")
    @Mapping(target = "originalLink", source = "sourceUrl")
    @Mapping(target = "interest", ignore = true)
    Articles toEntity(ArticleDto dto);

    // ⬇️ null-safe 변환용 default 메서드 추가
    default Articles safeToEntity(ArticleSaveRequest request, Interest interest) {
        if (request == null) {
            throw new IllegalArgumentException("ArticleSaveRequest must not be null");
        }

        return Articles.builder()
            .interest(interest)
            .source(request.source())
            .originalLink(request.originalLink())
            .title(request.title() != null ? request.title() : "[제목 없음]")
            .summary(request.summary() != null ? request.summary() : "[요약 없음]")
            .publishedAt(request.publishedAt() != null ? request.publishedAt() : LocalDateTime.now())
            .viewCount(0L)
            .commentCount(0L)
            .deleted(false)
            .build();
    }
}
