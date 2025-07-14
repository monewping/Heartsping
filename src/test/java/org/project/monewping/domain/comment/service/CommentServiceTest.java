package org.project.monewping.domain.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
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
import org.project.monewping.domain.comment.mapper.CommentMapper;
import org.project.monewping.domain.comment.repository.CommentRepository;
import org.project.monewping.global.dto.CursorPageResponse;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentService 테스트")
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentMapper commentMapper;

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

        // 테스트용 Comment 엔티티 생성
        testComments = Arrays.asList(
            Comment.builder()
                .id(UUID.randomUUID())
                .articleId(testArticleId)
                .userId(testUserId)
                .userNickname("사용자1")
                .content("첫 번째 댓글입니다.")
                .likeCount(5)
                .createdAt(LocalDateTime.now().minusHours(1))
                .updatedAt(LocalDateTime.now().minusHours(1))
                .deleted(false)
                .build(),
            Comment.builder()
                .id(UUID.randomUUID())
                .articleId(testArticleId)
                .userId(testUserId)
                .userNickname("사용자2")
                .content("두 번째 댓글입니다.")
                .likeCount(3)
                .createdAt(LocalDateTime.now().minusHours(2))
                .updatedAt(LocalDateTime.now().minusHours(2))
                .deleted(false)
                .build()
        );

        // 테스트용 CommentResponseDto 생성
        testResponseDtos = Arrays.asList(
            new CommentResponseDto(
                testComments.get(0).getId(),
                testComments.get(0).getContent(),
                testComments.get(0).getUserNickname(),
                testComments.get(0).getLikeCount(),
                testComments.get(0).getCreatedAt()
            ),
            new CommentResponseDto(
                testComments.get(1).getId(),
                testComments.get(1).getContent(),
                testComments.get(1).getUserNickname(),
                testComments.get(1).getLikeCount(),
                testComments.get(1).getCreatedAt()
            )
        );
    }

    @Test
    @DisplayName("댓글 조회 성공 - 기본 파라미터")
    void getComments_Success_WithDefaultParameters() {
        // Given
        when(commentRepository.findComments(
            eq(testArticleId),
            eq("createdAt"),
            eq("DESC"),
            eq(null),
            eq(null),
            eq(50)
        )).thenReturn(testComments);

        when(commentMapper.toResponseDto(testComments.get(0))).thenReturn(testResponseDtos.get(0));
        when(commentMapper.toResponseDto(testComments.get(1))).thenReturn(testResponseDtos.get(1));

        // When
        CursorPageResponse<CommentResponseDto> result = commentService.getComments(
            testArticleId, "createdAt", "DESC", null, null, 50
        );

        // Then
        assertThat(result.content()).hasSize(2);
        assertThat(result.content().get(0).getContent()).isEqualTo("첫 번째 댓글입니다.");
        assertThat(result.content().get(0).getUserNickname()).isEqualTo("사용자1");
        assertThat(result.content().get(0).getLikeCount()).isEqualTo(5);
        assertThat(result.content().get(1).getContent()).isEqualTo("두 번째 댓글입니다.");
        assertThat(result.content().get(1).getUserNickname()).isEqualTo("사용자2");
        assertThat(result.content().get(1).getLikeCount()).isEqualTo(3);
        assertThat(result.nextCursor()).isEqualTo(testComments.get(1).getId().toString());
        assertThat(result.nextIdAfter()).isEqualTo(Math.abs(testComments.get(1).getId().getMostSignificantBits()));
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.totalElements()).isEqualTo(2L);
        assertThat(result.hasNext()).isFalse(); // size != limit이므로 false
    }

    @Test
    @DisplayName("댓글 조회 성공 - 모든 파라미터 포함")
    void getComments_Success_WithAllParameters() {
        // Given
        String cursor = "test_cursor";
        String after = "2024-01-01T10:00:00";
        int limit = 20;

        when(commentRepository.findComments(
            eq(testArticleId),
            eq("likeCount"),
            eq("ASC"),
            eq(cursor),
            eq(after),
            eq(limit)
        )).thenReturn(testComments);

        when(commentMapper.toResponseDto(any(Comment.class)))
            .thenReturn(testResponseDtos.get(0))
            .thenReturn(testResponseDtos.get(1));

        // When
        CursorPageResponse<CommentResponseDto> result = commentService.getComments(
            testArticleId, "likeCount", "ASC", cursor, after, limit
        );

        // Then
        assertThat(result.content()).hasSize(2);
        assertThat(result.nextCursor()).isEqualTo(testComments.get(1).getId().toString());
        assertThat(result.hasNext()).isFalse(); // size != limit이므로 false
    }

    @Test
    @DisplayName("댓글 조회 성공 - 빈 결과")
    void getComments_Success_EmptyResult() {
        // Given
        when(commentRepository.findComments(
            any(UUID.class),
            any(String.class),
            any(String.class),
            any(),
            any(),
            any(Integer.class)
        )).thenReturn(Arrays.asList());

        // When
        CursorPageResponse<CommentResponseDto> result = commentService.getComments(
            testArticleId, "createdAt", "DESC", null, null, 50
        );

        // Then
        assertThat(result.content()).isEmpty();
        assertThat(result.nextCursor()).isNull();
        assertThat(result.nextIdAfter()).isNull();
        assertThat(result.size()).isEqualTo(0);
        assertThat(result.totalElements()).isEqualTo(0L);
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    @DisplayName("댓글 조회 성공 - hasNext가 true인 경우")
    void getComments_Success_HasNextTrue() {
        // Given
        int limit = 2;

        when(commentRepository.findComments(
            eq(testArticleId),
            eq("createdAt"),
            eq("DESC"),
            eq(null),
            eq(null),
            eq(limit)
        )).thenReturn(testComments);

        when(commentMapper.toResponseDto(any(Comment.class)))
            .thenReturn(testResponseDtos.get(0))
            .thenReturn(testResponseDtos.get(1));

        // When
        CursorPageResponse<CommentResponseDto> result = commentService.getComments(
            testArticleId, "createdAt", "DESC", null, null, limit
        );

        // Then
        assertThat(result.content()).hasSize(2);
        assertThat(result.hasNext()).isTrue(); // size == limit이므로 true
    }

    @Test
    @DisplayName("댓글 조회 성공 - 커서 기반 페이지네이션")
    void getComments_Success_WithCursor() {
        // Given
        String cursor = "cursor_value";

        when(commentRepository.findComments(
            eq(testArticleId),
            eq("createdAt"),
            eq("DESC"),
            eq(cursor),
            eq(null),
            eq(50)
        )).thenReturn(testComments);

        when(commentMapper.toResponseDto(any(Comment.class)))
            .thenReturn(testResponseDtos.get(0))
            .thenReturn(testResponseDtos.get(1));

        // When
        CursorPageResponse<CommentResponseDto> result = commentService.getComments(
            testArticleId, "createdAt", "DESC", cursor, null, 50
        );

        // Then
        assertThat(result.content()).hasSize(2);
        assertThat(result.nextCursor()).isEqualTo(testComments.get(1).getId().toString());
        assertThat(result.nextIdAfter()).isEqualTo(Math.abs(testComments.get(1).getId().getMostSignificantBits()));
    }

    @Test
    @DisplayName("댓글 조회 성공 - after 파라미터 사용")
    void getComments_Success_WithAfterParameter() {
        // Given
        String after = "2024-01-01T10:00:00";

        when(commentRepository.findComments(
            eq(testArticleId),
            eq("createdAt"),
            eq("DESC"),
            eq(null),
            eq(after),
            eq(50)
        )).thenReturn(testComments);

        when(commentMapper.toResponseDto(any(Comment.class)))
            .thenReturn(testResponseDtos.get(0))
            .thenReturn(testResponseDtos.get(1));

        // When
        CursorPageResponse<CommentResponseDto> result = commentService.getComments(
            testArticleId, "createdAt", "DESC", null, after, 50
        );

        // Then
        assertThat(result.content()).hasSize(2);
        assertThat(result.nextCursor()).isEqualTo(testComments.get(1).getId().toString());
    }

    @Test
    @DisplayName("댓글 조회 성공 - 정렬 기준 likeCount")
    void getComments_Success_OrderByLikeCount() {
        // Given
        when(commentRepository.findComments(
            eq(testArticleId),
            eq("likeCount"),
            eq("ASC"),
            eq(null),
            eq(null),
            eq(50)
        )).thenReturn(testComments);

        when(commentMapper.toResponseDto(any(Comment.class)))
            .thenReturn(testResponseDtos.get(0))
            .thenReturn(testResponseDtos.get(1));

        // When
        CursorPageResponse<CommentResponseDto> result = commentService.getComments(
            testArticleId, "likeCount", "ASC", null, null, 50
        );

        // Then
        assertThat(result.content()).hasSize(2);
        assertThat(result.nextCursor()).isEqualTo(testComments.get(1).getId().toString());
    }
    @Test
    @DisplayName("댓글 등록 성공")
    void registerComment_Success() {
        CommentRegisterRequestDto requestDto = new CommentRegisterRequestDto();
        requestDto.setArticleId(UUID.randomUUID());
        requestDto.setUserId(UUID.randomUUID());
        requestDto.setContent("테스트 댓글입니다.");

        Comment mockComment = Comment.builder()
            .id(UUID.randomUUID())
            .articleId(requestDto.getArticleId())
            .userId(requestDto.getUserId())
            .content(requestDto.getContent())
            .userNickname("테스트 유저")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .likeCount(0)
            .deleted(false)
            .build();

        when(commentMapper.toEntity(any(CommentRegisterRequestDto.class))).thenReturn(mockComment);

        commentService.registerComment(requestDto);

        verify(commentRepository).save(any(Comment.class));
    }

}