package org.project.monewping.domain.comment.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.monewping.domain.comment.domain.Comment;
import org.project.monewping.domain.comment.dto.CommentRegisterRequestDto;
import org.project.monewping.domain.comment.dto.CommentResponseDto;
import org.project.monewping.domain.comment.exception.CommentNotFoundException;
import org.project.monewping.domain.comment.mapper.CommentMapper;
import org.project.monewping.domain.comment.repository.CommentRepository;
import org.project.monewping.global.dto.CursorPageResponse;
import org.springframework.stereotype.Service;
import org.project.monewping.domain.comment.exception.CommentDeleteException;

import java.util.List;

/**
 * 댓글 서비스 구현체
 * 기사에 대한 댓글 목록을 조회하고 등록하는 비즈니스 로직을 제공합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    @Override
    public CursorPageResponse<CommentResponseDto> getComments(
        UUID articleId,
        String orderBy,
        String direction,
        String cursor,
        String after,
        int limit
    ) {
        List<Comment> comments = commentRepository.findComments(articleId, orderBy, direction, cursor, after, limit);
        List<CommentResponseDto> response = comments.stream()
            .map(commentMapper::toResponseDto)
            .toList();

        UUID lastId = comments.isEmpty() ? null : comments.get(comments.size() - 1).getId();

        Long nextIdAfter = lastId == null ? null : Math.abs(lastId.getMostSignificantBits());
        String nextCursor = lastId == null ? null : lastId.toString();

        int size = comments.size();
        long totalElements = commentRepository.countByArticleId(articleId);
        boolean hasNext = size == limit;

        log.info("[CommentService] 댓글 목록 조회 - articleId: {}, 조회 수: {}, total: {}", articleId, size, totalElements);

        return new CursorPageResponse<>(
            response,
            nextIdAfter,
            nextCursor,
            size,
            totalElements,
            hasNext
        );
    }
    // 댓글 등록
    @Override
    public void registerComment(CommentRegisterRequestDto requestDto) {
        Comment comment = commentMapper.toEntity(requestDto);
        commentRepository.save(comment);
        log.info("[CommentService] 댓글 등록 완료 - articleId: {}, userId: {}", requestDto.getArticleId(), requestDto.getUserId());
    }

    // 논리 삭제
    @Override
    public void deleteComment(UUID commentId, UUID userId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new CommentNotFoundException(commentId));

        if (!comment.getUserId().equals(userId)) {
            throw new CommentDeleteException("본인의 댓글만 삭제할 수 있습니다.");
        }

        comment.delete();
        commentRepository.save(comment);
        log.info("[CommentService] 댓글 논리 삭제 완료 - commentId: {}, userId: {}", commentId, userId);
    }

    // 물리 삭제
    @Override
    public void deleteCommentPhysically(UUID commentId, UUID userId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new CommentNotFoundException(commentId));

        if (!comment.getUserId().equals(userId)) {
            throw new CommentDeleteException("본인의 댓글만 삭제할 수 있습니다.");
        }

        commentRepository.delete(comment);
        log.info("[CommentService] 댓글 물리 삭제 완료 - commentId: {}, userId: {}", commentId, userId);
    }
}