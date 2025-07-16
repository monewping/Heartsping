package org.project.monewping.domain.article.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.project.monewping.domain.article.dto.data.ArticleViewDto;
import org.project.monewping.domain.article.entity.ArticleViews;

@Mapper(componentModel = "spring")
public interface ArticleViewsMapper {

    /**
     * Converts an {@link ArticleViewDto} to an {@link ArticleViews} entity, excluding the {@code article} field.
     * <p>
     * The {@code article} field is intentionally ignored and should be set separately after mapping.
     *
     * @param dto the data transfer object to convert
     * @return the mapped {@link ArticleViews} entity without the {@code article} field set
     */
    @Mapping(target = "article", ignore = true) // 엔티티 생성 시 article 필드는 별도 세팅하는 방식 권장
    ArticleViews toEntity(ArticleViewDto dto);

    /**
     * Converts an ArticleViews entity to an ArticleViewDto, mapping nested article fields to corresponding flat fields in the DTO.
     *
     * @param entity the ArticleViews entity to convert
     * @return the resulting ArticleViewDto with mapped article details
     */
    @Mapping(source = "article.id", target = "articleId")
    @Mapping(source = "article.source", target = "source")
    @Mapping(source = "article.originalLink", target = "sourceUrl")
    @Mapping(source = "article.title", target = "articleTitle")
    @Mapping(source = "article.publishedAt", target = "articlePublishedDate")
    @Mapping(source = "article.summary", target = "articleSummary")
    @Mapping(source = "article.commentCount", target = "articleCommentCount")
    @Mapping(source = "article.viewCount", target = "articleViewCount")
    ArticleViewDto toDto(ArticleViews entity);

}
