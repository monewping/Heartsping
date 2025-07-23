package org.project.monewping.domain.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.BDDMockito.given;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.monewping.domain.comment.domain.Comment;
import org.project.monewping.domain.comment.dto.CommentRegisterRequestDto;
import org.project.monewping.domain.comment.dto.CommentResponseDto;
import org.project.monewping.domain.comment.dto.CommentUpdateRequestDto;
import org.project.monewping.domain.comment.exception.CommentDeleteException;
import org.project.monewping.domain.comment.mapper.CommentMapper;
import org.project.monewping.domain.comment.repository.CommentRepository;
import org.project.monewping.domain.notification.repository.NotificationRepository;
import org.project.monewping.domain.user.domain.User;
import org.project.monewping.domain.user.repository.UserRepository;
import org.project.monewping.global.dto.CursorPageResponse;


@ExtendWith(MockitoExtension.class)
@DisplayName("CommentService 테스트")
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private CommentServiceImpl commentService;

    private UUID testArticleId;
    private UUID testUserId;
    private List<Comment> testComments;
    private List<CommentResponseDto> testResponseDtos;

    @BeforeEach
    void setUp() {
        testArticleId = UUID.randomUUID();
        testUserId = UUID.randomUUID();

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
                testComments.get(0).getId(),
                testComments.get(0).getArticleId(),
                testComments.get(0).getUserId(),
                testComments.get(0).getUserNickname(),
                testComments.get(0).getContent(),
                testComments.get(0).getLikeCount(),
                false,
                testComments.get(0).getCreatedAt().toString()
            ),
            new CommentResponseDto(
                testComments.get(1).getId(),
                testComments.get(1).getArticleId(),
                testComments.get(1).getUserId(),
                testComments.get(1).getUserNickname(),
                testComments.get(1).getContent(),
                testComments.get(1).getLikeCount(),
                false,
                testComments.get(1).getCreatedAt().toString()
            )
        );
    }
    @Test
    @DisplayName("댓글 조회 성공 - createdAt 기준 기본 파라미터")
    void getComments_Success_WithCreatedAtCursor() {
        int limit = 51; // 내부적으로 limit+1

        when(commentRepository.findCommentsByCreatedAtCursor(
            eq(testArticleId),
            eq(null),
            eq(limit)
        )).thenReturn(testComments);

        when(commentRepository.countByArticleId(testArticleId)).thenReturn((long) testComments.size());
        for (int i = 0; i < testComments.size(); i++) {
            when(commentMapper.toResponseDto(testComments.get(i))).thenReturn(testResponseDtos.get(i));
        }

        CursorPageResponse<CommentResponseDto> result = commentService.getComments(
            testArticleId, "createdAt", "DESC", null, null, limit - 1
        );

        assertThat(result.content()).hasSize(testComments.size());
        assertThat(result.nextCursor()).isEqualTo(testComments.get(testComments.size() - 1).getCreatedAt().toString());
        assertThat(result.size()).isEqualTo(testComments.size());
        assertThat(result.totalElements()).isEqualTo((long) testComments.size());
        assertThat(result.hasNext()).isFalse();
    }


    @Test
    @DisplayName("댓글 조회 성공 - likeCount 기준 커서 조회")
    void getComments_Success_WithLikeCountCursor() {
        int limit = 20;
        int queryLimit = limit + 1;

        UUID articleId = testArticleId;

        when(commentRepository.findCommentsByLikeCountCursor(
            eq(articleId),
            eq(null),
            eq(queryLimit)
        )).thenReturn(testComments);

        when(commentRepository.countByArticleId(articleId)).thenReturn((long) testComments.size());

        for (int i = 0; i < testComments.size(); i++) {
            when(commentMapper.toResponseDto(testComments.get(i))).thenReturn(testResponseDtos.get(i));
        }

        CursorPageResponse<CommentResponseDto> result = commentService.getComments(
            articleId, "likeCount", "ASC", null, null, limit
        );

        assertThat(result.content()).hasSize(testComments.size());
        assertThat(result.nextCursor()).isEqualTo(String.valueOf(testComments.get(testComments.size() - 1).getLikeCount()));
        assertThat(result.size()).isEqualTo(testComments.size());
        assertThat(result.totalElements()).isEqualTo((long) testComments.size());
        assertThat(result.hasNext()).isFalse();
    }


    @Test
    @DisplayName("댓글 등록 성공")
    void registerComment_Success() {
        UUID articleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String userNickname = "테스트 유저";

        CommentRegisterRequestDto requestDto = new CommentRegisterRequestDto();
        requestDto.setArticleId(articleId);
        requestDto.setUserId(userId);
        requestDto.setContent("테스트 댓글입니다.");

        User mockUser = User.builder()
            .id(userId)
            .nickname(userNickname)
            .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        when(commentMapper.toEntity(eq(requestDto), eq(userNickname)))
            .thenReturn(Comment.builder()
                .articleId(articleId)
                .userId(userId)
                .userNickname(userNickname)
                .content("테스트 댓글입니다.")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .likeCount(0)
                .isDeleted(false)
                .build());

        commentService.registerComment(requestDto);

        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    @DisplayName("댓글 조회 성공 - 빈 결과")
    void getComments_Success_EmptyResult() {
        when(commentRepository.findCommentsByCreatedAtCursor(
            any(UUID.class),
            any(),
            anyInt()
        )).thenReturn(List.of());

        when(commentRepository.countByArticleId(testArticleId)).thenReturn(0L);

        CursorPageResponse<CommentResponseDto> result = commentService.getComments(
            testArticleId, "createdAt", "DESC", null, null, 50
        );

        assertThat(result.content()).isEmpty();
        assertThat(result.nextCursor()).isNull();
        assertThat(result.nextAfter()).isNull();
        assertThat(result.size()).isEqualTo(0);
        assertThat(result.totalElements()).isEqualTo(0L);
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    @DisplayName("댓글 논리 삭제 성공")
    void deleteComment_Success() {
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Comment comment = Comment.builder()
            .id(commentId)
            .userId(userId)
            .isDeleted(false)
            .updatedAt(Instant.now())
            .build();

        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        doNothing().when(notificationRepository)
            .deactivateByResourceId(commentId);
        given(notificationRepository.findByResourceIdAndActiveFalse(commentId))
            .willReturn(Collections.emptyList());

        commentService.deleteComment(commentId, userId);

        assertThat(comment.getIsDeleted()).isTrue();
        verify(commentRepository).save(comment);

        verify(notificationRepository).deactivateByResourceId(commentId);
        verify(notificationRepository).findByResourceIdAndActiveFalse(commentId);
    }

    @Test
    @DisplayName("댓글 논리 삭제 실패 - 본인 아님")
    void deleteComment_Fail_NotOwner() {
        UUID commentId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID attackerId = UUID.randomUUID();

        Comment comment = Comment.builder()
            .id(commentId)
            .userId(ownerId)
            .isDeleted(false)
            .updatedAt(Instant.now())
            .build();

        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.deleteComment(commentId, attackerId))
            .isInstanceOf(CommentDeleteException.class)
            .hasMessageContaining("본인의 댓글만 삭제할 수 있습니다.");
    }

    @Test
    @DisplayName("댓글 물리 삭제 성공")
    void deleteCommentPhysically_Success() {
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Comment comment = Comment.builder()
            .id(commentId)
            .userId(userId)
            .isDeleted(false)
            .updatedAt(Instant.now())
            .build();

        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        doNothing().when(notificationRepository)
            .deactivateByResourceId(commentId);
        given(notificationRepository.findByResourceIdAndActiveFalse(commentId))
            .willReturn(Collections.emptyList());

        commentService.deleteCommentPhysically(commentId, userId);

        verify(commentRepository).delete(comment);
        verify(notificationRepository).deactivateByResourceId(commentId);
        verify(notificationRepository).findByResourceIdAndActiveFalse(commentId);
    }

    @Test
    @DisplayName("댓글 물리 삭제 실패 - 본인 아님")
    void deleteCommentPhysically_Fail_NotOwner() {
        UUID commentId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID attackerId = UUID.randomUUID();

        Comment comment = Comment.builder()
            .id(commentId)
            .userId(ownerId)
            .isDeleted(false)
            .updatedAt(Instant.now())
            .build();

        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.deleteCommentPhysically(commentId, attackerId))
            .isInstanceOf(CommentDeleteException.class)
            .hasMessageContaining("본인의 댓글만 삭제할 수 있습니다.");
    }

    @DisplayName("댓글 수정 성공")
    @Test
    void updateComment_Success() {
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Comment comment = Comment.builder()
            .id(commentId)
            .userId(userId)
            .content("기존 댓글입니다.")
            .isDeleted(false)
            .updatedAt(Instant.now())
            .build();

        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        CommentUpdateRequestDto request = new CommentUpdateRequestDto("수정된 댓글입니다.");

        commentService.updateComment(commentId, userId, request);

        assertThat(comment.getContent()).isEqualTo("수정된 댓글입니다.");
        verify(commentRepository).findById(commentId);
    }

    @DisplayName("댓글 수정 실패 - 본인 아님")
    @Test
    void updateComment_Fail_NotOwner() {
        UUID commentId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID attackerId = UUID.randomUUID();

        Comment comment = Comment.builder()
            .id(commentId)
            .userId(ownerId)
            .content("기존 댓글입니다.")
            .isDeleted(false)
            .updatedAt(Instant.now())
            .build();

        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        CommentUpdateRequestDto request = new CommentUpdateRequestDto("수정된 댓글입니다.");

        assertThatThrownBy(() -> commentService.updateComment(commentId, attackerId, request))
            .isInstanceOf(CommentDeleteException.class)
            .hasMessageContaining("본인의 댓글만 수정할 수 있습니다.");
    }

    @DisplayName("댓글 수정 실패 - 삭제된 댓글")
    @Test
    void updateComment_Fail_Deleted() {
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Comment comment = Comment.builder()
            .id(commentId)
            .userId(userId)
            .content("기존 댓글입니다.")
            .isDeleted(true)
            .updatedAt(Instant.now())
            .build();

        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        CommentUpdateRequestDto request = new CommentUpdateRequestDto("수정된 댓글입니다.");

        assertThatThrownBy(() -> commentService.updateComment(commentId, userId, request))
            .isInstanceOf(CommentDeleteException.class)
            .hasMessageContaining("삭제된 댓글은 수정할 수 없습니다.");
    }
}
