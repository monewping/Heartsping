package org.project.monewping.domain.article.mapper;

import org.mapstruct.Mapper;
import org.project.monewping.domain.article.dto.data.ArticleViewDto;
import org.project.monewping.domain.article.entity.ArticleViews;

@Mapper(componentModel = "spring")
public interface ArticleViewsMapper {

    ArticleViews toEntity(ArticleViewDto dto);

    ArticleViewDto toDto(ArticleViews entity);

}
