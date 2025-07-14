package org.project.monewping.domain.article.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.project.monewping.domain.article.dto.data.ArticleViewDto;
import org.project.monewping.domain.article.entity.NewsViewHistory;

@Mapper(componentModel = "spring")
public interface NewsViewHistoryMapper {

    NewsViewHistory toEntity(ArticleViewDto dto);

    ArticleViewDto toDto(NewsViewHistory entity);

}
