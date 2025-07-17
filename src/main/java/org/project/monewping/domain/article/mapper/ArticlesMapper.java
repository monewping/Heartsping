package org.project.monewping.domain.article.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
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

}
