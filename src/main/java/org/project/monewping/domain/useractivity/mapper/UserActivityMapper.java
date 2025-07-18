package org.project.monewping.domain.useractivity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.project.monewping.domain.useractivity.document.UserActivityDocument;
import org.project.monewping.domain.useractivity.dto.UserActivityDto;

/**
 * 사용자 활동 내역 Document와 DTO 간의 매핑을 담당하는 Mapper
 */
@Mapper(componentModel = "spring")
public interface UserActivityMapper {

    /**
     * UserActivityDocument를 UserActivityDto로 변환
     */
    @Mapping(source = "userId", target = "user.id")
    UserActivityDto toDto(UserActivityDocument document);

    /**
     * UserActivityDocument.UserInfo를 UserActivityDto.UserInfo로 변환
     */
    UserActivityDto.UserInfo toUserInfoDto(UserActivityDocument.UserInfo userInfo);

    /**
     * UserActivityDocument.SubscriptionInfo를 UserActivityDto.SubscriptionDto로 변환
     */
    UserActivityDto.SubscriptionDto toSubscriptionDto(UserActivityDocument.SubscriptionInfo subscriptionInfo);

    /**
     * UserActivityDocument.CommentInfo를 UserActivityDto.CommentDto로 변환
     */
    UserActivityDto.CommentDto toCommentDto(UserActivityDocument.CommentInfo commentInfo);

    /**
     * UserActivityDocument.CommentLikeInfo를 UserActivityDto.CommentLikeDto로 변환
     */
    UserActivityDto.CommentLikeDto toCommentLikeDto(UserActivityDocument.CommentLikeInfo commentLikeInfo);

    /**
     * UserActivityDocument.ArticleViewInfo를 UserActivityDto.ArticleViewDto로 변환
     */
    UserActivityDto.ArticleViewDto toArticleViewDto(UserActivityDocument.ArticleViewInfo articleViewInfo);
}