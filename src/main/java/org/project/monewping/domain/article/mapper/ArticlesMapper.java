package org.project.monewping.domain.article.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.project.monewping.domain.article.dto.data.ArticleDto;
import org.project.monewping.domain.article.dto.data.ArticleViewDto;
import org.project.monewping.domain.article.dto.request.ArticleSaveRequest;
import org.project.monewping.domain.article.entity.Articles;
import org.project.monewping.domain.article.entity.Interest;

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
    ArticleDto toDto(Articles article);

    // 복구용 DTO → 엔티티
    @Mapping(source = "sourceUrl", target = "originalLink")
    @Mapping(source = "publishDate", target = "publishedAt")
    @Mapping(target = "deleted", constant = "false")
    @Mapping(target = "interest", ignore = true)
    Articles toEntity(ArticleDto dto);

}
