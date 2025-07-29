package org.project.monewping.domain.comment.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.monewping.domain.comment.entity.Comment;
import org.project.monewping.domain.comment.entity.CommentLike;
import org.project.monewping.domain.comment.repository.CommentLikeRepository;
import org.project.monewping.domain.comment.repository.CommentRepository;
import org.project.monewping.domain.notification.repository.NotificationRepository;
import org.project.monewping.domain.user.entity.User;
import org.project.monewping.domain.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentLikeService 테스트")
class CommentLikeServiceTest {

    @Mock
    private CommentLikeRepository commentLikeRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private CommentLikeService commentLikeService;

    private UUID userId;
    private UUID commentId;
    private User user;
    private Comment comment;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        commentId = UUID.randomUUID();
        user = User.builder()
            .id(userId)
            .email("test@example.com")
            .nickname("테스트유저")
            .password("1234")
            .isDeleted(false)
            .build();
        comment = Comment.builder()
            .id(commentId)
            .userId(userId)
            .content("테스트 댓글")
            .userNickname("테스트유저")
            .likeCount(0)
            .articleId(UUID.randomUUID())
            .isDeleted(false)
            .build();
    }

    @Test
    @DisplayName("댓글 좋아요 등록 후 알림 생성 성공")
    void likeComment_Success() {
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
        given(commentLikeRepository.existsByUserAndComment(user, comment)).willReturn(false);

        commentLikeService.likeComment(userId, commentId);

        verify(commentLikeRepository).save(any(CommentLike.class));

        verify(notificationRepository).save(argThat(n ->
            n.getUserId().equals(comment.getUserId()) &&
            n.getResourceId().equals(comment.getId()) &&
            n.getContent().contains(user.getNickname())
        ));
    }

    @Test
    @DisplayName("댓글 좋아요 취소 성공")
    void unlikeComment_Success() {
        CommentLike commentLike = CommentLike.builder()
            .user(user)
            .comment(comment)
            .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
        given(commentLikeRepository.findByUserAndComment(user, comment)).willReturn(Optional.of(commentLike));

        commentLikeService.unlikeComment(userId, commentId);

        verify(commentLikeRepository).delete(commentLike);
    }

}