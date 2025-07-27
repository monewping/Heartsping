package org.project.monewping.domain.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.monewping.domain.article.entity.Articles;
import org.project.monewping.domain.article.repository.ArticlesRepository;
import org.project.monewping.domain.comment.domain.Comment;
import org.project.monewping.domain.comment.dto.CommentRegisterRequestDto;
import org.project.monewping.domain.comment.dto.CommentResponseDto;
import org.project.monewping.domain.comment.dto.CommentUpdateRequestDto;
import org.project.monewping.domain.comment.exception.CommentDeleteException;
import org.project.monewping.domain.comment.mapper.CommentMapper;
import org.project.monewping.domain.comment.repository.CommentLikeRepository;
import org.project.monewping.domain.comment.repository.CommentRepository;
import org.project.monewping.domain.notification.entity.Notification;
import org.project.monewping.domain.notification.repository.NotificationRepository;
import org.project.monewping.domain.user.domain.User;
import org.project.monewping.domain.user.repository.UserRepository;
import org.project.monewping.global.dto.CursorPageResponse;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentService 테스트")
class CommentServiceTest {

    @Mock private CommentRepository commentRepository;
    @Mock private CommentMapper commentMapper;
    @Mock private UserRepository userRepository;
    @Mock private ArticlesRepository articlesRepository;
    @Mock private CommentLikeRepository commentLikeRepository;
    @Mock private NotificationRepository notificationRepository;

    @InjectMocks private CommentServiceImpl commentService;

    private UUID testArticleId;
    private UUID testUserId;
    private UUID testCommentId;
    private List<Comment> testComments;
    private List<CommentResponseDto> testResponseDtos;

    @BeforeEach
    void setUp() {
        testArticleId = UUID.randomUUID();
        testUserId = UUID.randomUUID();
        testCommentId = UUID.randomUUID();

        testComments = Arrays.asList(
            Comment.builder()
                .id(UUID.randomUUID())
                .articleId(testArticleId)
                .userId(testUserId)
                .userNickname("사용자1")
                .content("첫 번째 댓글입니다.")
                .likeCount(5)
                .createdAt(Instant.now().minus(Duration.ofHours(1)))
                .updatedAt(Instant.now().minus(Duration.ofHours(1)))
                .isDeleted(false)
                .build(),
            Comment.builder()
                .id(UUID.randomUUID())
                .articleId(testArticleId)
                .userId(testUserId)
                .userNickname("사용자2")
                .content("두 번째 댓글입니다.")
                .likeCount(3)
                .createdAt(Instant.now().minus(Duration.ofHours(2)))
                .updatedAt(Instant.now().minus(Duration.ofHours(2)))
                .isDeleted(false)
                .build()
        );

        testResponseDtos = Arrays.asList(
            new CommentResponseDto(
                testComments.get(0).getId(), testComments.get(0).getArticleId(),
                testComments.get(0).getUserId(), testComments.get(0).getUserNickname(),
                testComments.get(0).getContent(), testComments.get(0).getLikeCount(),
                false, testComments.get(0).getCreatedAt().toString()
            ),
            new CommentResponseDto(
                testComments.get(1).getId(), testComments.get(1).getArticleId(),
                testComments.get(1).getUserId(), testComments.get(1).getUserNickname(),
                testComments.get(1).getContent(), testComments.get(1).getLikeCount(),
                false, testComments.get(1).getCreatedAt().toString()
            )
        );
    }

    @Test
    @DisplayName("댓글 조회 성공 - createdAt 기준")
    void getComments_ByCreatedAt_Success() {
        int limit = 50;
        when(commentRepository.findCommentsByCreatedAtCursor(eq(testArticleId), eq(null), eq(limit + 1)))
            .thenReturn(testComments);
        when(commentRepository.countByArticleId(testArticleId)).thenReturn((long) testComments.size());
        when(commentLikeRepository.findCommentIdsByUserIdAndArticleId(testUserId, testArticleId)).thenReturn(Set.of());

        for (int i = 0; i < testComments.size(); i++) {
            when(commentMapper.toResponseDto(testComments.get(i), false)).thenReturn(testResponseDtos.get(i));
        }

        CursorPageResponse<CommentResponseDto> response = commentService.getComments(
            testArticleId, "createdAt", "DESC", null, null, limit, testUserId
        );

        assertThat(response.content()).hasSize(2);
        assertThat(response.hasNext()).isFalse();
        assertThat(response.nextCursor()).isEqualTo(testComments.get(1).getCreatedAt().toString());
    }

    @Test
    @DisplayName("댓글 조회 성공 - likeCount 기준")
    void getComments_ByLikeCount_Success() {
        int limit = 20;
        when(commentRepository.findCommentsByLikeCountCursor(eq(testArticleId), eq(null), eq(limit + 1)))
            .thenReturn(testComments);
        when(commentRepository.countByArticleId(testArticleId)).thenReturn((long) testComments.size());
        when(commentLikeRepository.findCommentIdsByUserIdAndArticleId(testUserId, testArticleId)).thenReturn(Set.of());

        for (int i = 0; i < testComments.size(); i++) {
            when(commentMapper.toResponseDto(testComments.get(i), false)).thenReturn(testResponseDtos.get(i));
        }

        CursorPageResponse<CommentResponseDto> response = commentService.getComments(
            testArticleId, "likeCount", "ASC", null, null, limit, testUserId
        );

        assertThat(response.content()).hasSize(2);
        assertThat(response.hasNext()).isFalse();
        assertThat(response.nextCursor()).isEqualTo(String.valueOf(testComments.get(1).getLikeCount()));
    }

    @Test
    @DisplayName("댓글 등록 성공 - 기사 댓글 수 증가")
    void registerComment_IncreaseCommentCount() {
        UUID articleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String nickname = "테스트유저";

        CommentRegisterRequestDto dto = new CommentRegisterRequestDto();
        dto.setArticleId(articleId);
        dto.setUserId(userId);
        dto.setContent("내용");

        Articles article = Articles.builder().commentCount(0L).deleted(false).build();
        User user = User.builder().id(userId).nickname(nickname).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(articlesRepository.findById(articleId)).thenReturn(Optional.of(article));
        when(commentMapper.toEntity(dto, nickname)).thenReturn(
            Comment.builder()
                .articleId(articleId).userId(userId).userNickname(nickname)
                .content("내용").createdAt(Instant.now()).updatedAt(Instant.now())
                .likeCount(0).isDeleted(false).build()
        );

        commentService.registerComment(dto);
        assertThat(article.getCommentCount()).isEqualTo(1L);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    @DisplayName("댓글 논리 삭제 성공")
    void deleteComment_Logical_Success() {
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();

        Comment comment = Comment.builder().id(commentId).userId(userId).articleId(articleId).isDeleted(false).build();
        Articles article = Articles.builder().commentCount(1L).deleted(false).build();

        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
        given(articlesRepository.findById(articleId)).willReturn(Optional.of(article));

        Notification notification = Notification.builder()
            .id(UUID.randomUUID())
            .resourceId(commentId)
            .active(true)
            .build();

        given(notificationRepository.findByResourceIdAndActiveTrue(commentId))
            .willReturn(List.of(notification));

        doNothing().when(notificationRepository)
            .deactivateByResourceId(commentId);

        commentService.deleteComment(commentId, userId);

        then(notificationRepository).should()
            .findByResourceIdAndActiveTrue(commentId);

        then(notificationRepository).should()
            .deactivateByResourceId(commentId);

        assertThat(comment.getIsDeleted()).isTrue();
        assertThat(article.getCommentCount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("댓글 논리 삭제 실패 - 본인 아님")
    void deleteComment_NotOwner_Fail() {
        UUID commentId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID otherUser = UUID.randomUUID();
        Comment comment = Comment.builder().id(commentId).userId(ownerId).isDeleted(false).build();
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
        assertThatThrownBy(() -> commentService.deleteComment(commentId, otherUser))
            .isInstanceOf(CommentDeleteException.class)
            .hasMessageContaining("본인의 댓글만 삭제할 수 있습니다.");
    }

    @Test
    @DisplayName("댓글 물리 삭제 성공")
    void deleteComment_Physical_Success() {
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();

        Comment comment = Comment.builder().id(commentId).userId(userId).articleId(articleId).isDeleted(false).build();
        Articles article = Articles.builder().commentCount(1L).deleted(false).build();

        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
        given(articlesRepository.findById(articleId)).willReturn(Optional.of(article));

        Notification likeNotification = Notification.builder()
            .id(UUID.randomUUID())
            .resourceId(commentId)
            .active(true)
            .build();
        given(notificationRepository.findByResourceIdAndActiveTrue(commentId))
            .willReturn(List.of(likeNotification));

        doNothing().when(notificationRepository).deactivateByResourceId(commentId);

        commentService.deleteCommentPhysically(commentId, userId);
        verify(commentRepository).delete(comment);

        assertThat(article.getCommentCount()).isEqualTo(0L);
        verify(notificationRepository).findByResourceIdAndActiveTrue(commentId);
        verify(notificationRepository).deactivateByResourceId(commentId);
    }

    @Test
    @DisplayName("댓글 수정 성공")
    void updateComment_Success() {
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Comment comment = Comment.builder().id(commentId).userId(userId).content("old").isDeleted(false).build();
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        CommentUpdateRequestDto dto = new CommentUpdateRequestDto("new content");
        commentService.updateComment(commentId, userId, dto);
        assertThat(comment.getContent()).isEqualTo("new content");
    }

    @Test
    @DisplayName("댓글 수정 실패 - 삭제된 댓글")
    void updateComment_Deleted_Fail() {
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Comment comment = Comment.builder().id(commentId).userId(userId).content("old").isDeleted(true).build();
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        CommentUpdateRequestDto dto = new CommentUpdateRequestDto("new content");
        assertThatThrownBy(() -> commentService.updateComment(commentId, userId, dto))
            .isInstanceOf(CommentDeleteException.class)
            .hasMessageContaining("삭제된 댓글은 수정할 수 없습니다.");
    }

    @Test
    @DisplayName("댓글 수정 실패 - 본인 아님")
    void updateComment_NotOwner_Fail() {
        UUID commentId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID otherUser = UUID.randomUUID();
        Comment comment = Comment.builder().id(commentId).userId(ownerId).content("old").isDeleted(false).build();
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        CommentUpdateRequestDto dto = new CommentUpdateRequestDto("new content");
        assertThatThrownBy(() -> commentService.updateComment(commentId, otherUser, dto))
            .isInstanceOf(CommentDeleteException.class)
            .hasMessageContaining("본인의 댓글만 수정할 수 있습니다.");
    }
}