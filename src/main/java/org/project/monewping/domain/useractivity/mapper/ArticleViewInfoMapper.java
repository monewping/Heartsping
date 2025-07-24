package org.project.monewping.domain.useractivity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.project.monewping.domain.article.entity.ArticleViews;
import org.project.monewping.domain.article.entity.Articles;
import org.project.monewping.domain.useractivity.document.UserActivityDocument.ArticleViewInfo;

@Mapper(componentModel = "spring")
public interface ArticleViewInfoMapper {

  /**
   * ArticleViews와 Articles 엔티티를 UserActivityDocument.ArticleViewInfo로 매핑
   *
   * @param articleViews 기사 조회 엔티티
   * @param article 기사 엔티티
   * @return UserActivityDocument.ArticleViewInfo
   */
  @Mapping(source = "articleViews.id", target = "id")
  @Mapping(source = "articleViews.viewedBy", target = "viewedBy")
  @Mapping(expression = "java(articleViews.getCreatedAt().toInstant(ZoneOffset.UTC))", target = "createdAt")
  @Mapping(source = "article.id", target = "articleId")
  @Mapping(source = "article.source", target = "source")
  @Mapping(source = "article.originalLink", target = "sourceUrl")
  @Mapping(source = "article.title", target = "articleTitle")
  @Mapping(expression = "java(article.getPublishedAt().toInstant(ZoneOffset.UTC))", target = "articlePublishedDate")
  @Mapping(source = "article.summary", target = "articleSummary")
  @Mapping(source = "article.commentCount", target = "articleCommentCount")
  @Mapping(source = "article.viewCount", target = "articleViewCount")
  ArticleViewInfo toArticleViewInfo(ArticleViews articleViews, Articles article);
} 