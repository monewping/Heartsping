package org.project.monewping.domain.article.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.project.monewping.domain.article.dto.request.ArticleSaveRequest;
import org.project.monewping.domain.article.entity.Articles;
import org.project.monewping.domain.article.entity.Interest;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface ArticlesMapper {

    /**
     * Converts an {@link ArticleSaveRequest} and an {@link Interest} entity into an {@link Articles} entity.
     * <p>
     * The resulting entity will have its {@code interest} field set from the provided parameter, {@code viewCount} initialized to 0, and {@code deleted} set to {@code false}.
     *
     * @param request  the article save request containing article data
     * @param interest the interest entity to associate with the article
     * @return a new {@link Articles} entity populated from the request and interest, with default values for view count and deletion status
     */
    @Mapping(target = "interest", source = "interest")
    @Mapping(target = "viewCount", constant = "0L")
    @Mapping(target = "deleted", constant = "false")
    Articles toEntity(ArticleSaveRequest request, Interest interest);

}
