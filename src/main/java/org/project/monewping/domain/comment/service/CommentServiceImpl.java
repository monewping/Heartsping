package org.project.monewping.domain.comment.service;

import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.monewping.domain.comment.domain.Comment;
import org.project.monewping.domain.comment.dto.CommentRegisterRequestDto;
import org.project.monewping.domain.comment.dto.CommentResponseDto;
import org.project.monewping.domain.comment.dto.CommentUpdateRequestDto;
import org.project.monewping.domain.comment.exception.CommentNotFoundException;
import org.project.monewping.domain.comment.mapper.CommentMapper;
import org.project.monewping.domain.comment.repository.CommentRepository;
import org.project.monewping.domain.user.domain.User;
import org.project.monewping.domain.user.exception.UserNotFoundException;
import org.project.monewping.domain.user.repository.UserRepository;
import org.project.monewping.global.dto.CursorPageResponse;
import org.springframework.stereotype.Service;
import org.project.monewping.domain.comment.exception.CommentDeleteException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 댓글 서비스 구현체
 * 기사에 대한 댓글 목록을 조회하고 등록하는 비즈니스 로직을 제공합니다.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final UserRepository userRepository;

    @Override
    public CursorPageResponse<CommentResponseDto> getComments(
        UUID articleId,
        String orderBy,
        String direction,
        String cursor,
        String after,
        int limit
    ) {
        // limit 기본값 및 최대 제한
        if (limit <= 0) limit = 50;
        if (limit > 100) limit = 100;

        List<Comment> comments;

        switch (orderBy) {
            case "likeCount" -> {
                Integer afterLikeCount = null;
                if (after != null && !after.isBlank()) {
                    try {
                        afterLikeCount = Integer.valueOf(after);
                    } catch (NumberFormatException e) {
                        afterLikeCount = null;
                    }
                }
                comments = commentRepository.findCommentsByLikeCountCursor(articleId, afterLikeCount, limit + 1);
            }
            case "createdAt" -> {
                Instant afterCreatedAt = null;
                if (after != null && !after.isBlank()) {
                    try {
                        afterCreatedAt = Instant.parse(after);
                    } catch (Exception e) {
                        afterCreatedAt = null;
                    }
                }
                comments = commentRepository.findCommentsByCreatedAtCursor(articleId, afterCreatedAt, limit + 1);
            }
            default -> {
                // 기본: createdAt 내림차순
                Instant afterCreatedAt = null;
                if (after != null && !after.isBlank()) {
                    try {
                        afterCreatedAt = Instant.parse(after);
                    } catch (Exception e) {
                        afterCreatedAt = null;
                    }
                }
                comments = commentRepository.findCommentsByCreatedAtCursor(articleId, afterCreatedAt, limit + 1);
            }
        }

        boolean hasNext = comments.size() > limit;
        List<Comment> page = hasNext ? comments.subList(0, limit) : comments;

        List<CommentResponseDto> response = page.stream()
            .map(commentMapper::toResponseDto)
            .toList();

        int size = page.size();
        long totalElements = commentRepository.countByArticleId(articleId);

        String nextAfter = null;
        if (!page.isEmpty()) {
            Comment last = page.get(size - 1);
            nextAfter = switch (orderBy) {
                case "likeCount" -> String.valueOf(last.getLikeCount());
                case "createdAt" -> last.getCreatedAt().toString();
                default -> last.getCreatedAt().toString();
            };
        }

        return new CursorPageResponse<>(
            response,
            nextAfter,
            nextAfter,  // nextAfter에 커서 값 전달
            size,
            totalElements,
            hasNext
        );
    }

        // 댓글 등록
    @Override
    public CommentResponseDto registerComment(CommentRegisterRequestDto requestDto) {
        User user = userRepository.findById(requestDto.getUserId())
            .orElseThrow(() -> new UserNotFoundException(
                "해당 사용자를 찾을 수 없습니다. userId: " + requestDto.getUserId()
            ));

        Comment comment = commentMapper.toEntity(requestDto, user.getNickname());
        Comment saved = commentRepository.save(comment);

        log.info("[CommentService] 댓글 등록 완료 - articleId: {}, userId: {}, userNickname: {}",
            requestDto.getArticleId(), requestDto.getUserId(), user.getNickname());

        return commentMapper.toResponseDto(saved);
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

    // 댓글 수정
    @Override
    public CommentResponseDto updateComment(UUID commentId, UUID userId, CommentUpdateRequestDto request) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new CommentNotFoundException(commentId));

        if (comment.isDeleted()) {
            throw new CommentDeleteException("삭제된 댓글은 수정할 수 없습니다.");
        }

        if (!comment.getUserId().equals(userId)) {
            throw new CommentDeleteException("본인의 댓글만 수정할 수 있습니다.");
        }

        comment.updateContent(request.content());

        log.info("[CommentService] 댓글 수정 완료 - commentId: {}, userId: {}", commentId, userId);
        return commentMapper.toResponseDto(comment);
    }

    private String encodeCursor(Object orderValue, UUID id) {
        return orderValue + "_" + id.toString();
    }

}