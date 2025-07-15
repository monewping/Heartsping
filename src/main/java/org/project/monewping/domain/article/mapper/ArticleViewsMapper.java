package org.project.monewping.domain.article.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.project.monewping.domain.article.dto.data.ArticleViewDto;
import org.project.monewping.domain.article.entity.ArticleViews;

@Mapper(componentModel = "spring")
public interface ArticleViewsMapper {

    @Mapping(target = "article", ignore = true) // 엔티티 생성 시 article 필드는 별도 세팅하는 방식 권장
    ArticleViews toEntity(ArticleViewDto dto);

    @Mapping(source = "article.id", target = "articleId")
    @Mapping(source = "article.source", target = "source")
    @Mapping(source = "article.originalLink", target = "sourceUrl")
    @Mapping(source = "article.title", target = "articleTitle")
    @Mapping(source = "article.publishedAt", target = "articlePublishedAt")
    @Mapping(source = "article.summary", target = "articleSummary")
    @Mapping(source = "article.commentCount", target = "articleCommentCount")
    @Mapping(source = "article.viewCount", target = "articleViewCount")
    ArticleViewDto toDto(ArticleViews entity);

}
