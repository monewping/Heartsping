package org.project.monewping.domain.article.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.project.monewping.domain.article.dto.request.ArticleSaveRequest;
import org.project.monewping.domain.article.entity.Articles;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface ArticlesMapper {

    @Mapping(target = "articleId", expression = "java(UUID.randomUUID())")
    @Mapping(target = "interest", source = "interest")
    @Mapping(target = "viewCount", constant = "0")
    @Mapping(target = "deleted", constant = "false")
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(LocalDateTime.now())")
    Articles toEntity(ArticleSaveRequest request, Interest interest);

}
