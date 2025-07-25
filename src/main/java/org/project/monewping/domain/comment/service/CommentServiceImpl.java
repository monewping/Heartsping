package org.project.monewping.domain.comment.service;

import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.monewping.domain.article.entity.Articles;
import org.project.monewping.domain.article.repository.ArticlesRepository;
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
import org.project.monewping.domain.useractivity.document.UserActivityDocument;
import org.project.monewping.domain.useractivity.service.UserActivityService;
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
    private final ArticlesRepository articlesRepository;
    private final UserActivityService userActivityService;

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

        // 🔥 기사 댓글 수 증가
        Articles article = articlesRepository.findById(requestDto.getArticleId())
            .orElseThrow(() -> new RuntimeException("해당 기사를 찾을 수 없습니다. articleId: " + requestDto.getArticleId()));
        article.increaseCommentCount();

        Comment comment = commentMapper.toEntity(requestDto, user.getNickname());
        Comment saved = commentRepository.save(comment);

        log.debug("[CommentService] 댓글 등록 완료 - articleId: {}, userId: {}, userNickname: {}",
            requestDto.getArticleId(), requestDto.getUserId(), user.getNickname());

        // 사용자 활동 내역에 댓글 추가
        try {
            UserActivityDocument.CommentInfo commentInfo = UserActivityDocument.CommentInfo.builder()
                .id(saved.getId())
                .articleId(requestDto.getArticleId())
                .articleTitle(article.getTitle())
                .userId(requestDto.getUserId())
                .userNickname(user.getNickname())
                .content(saved.getContent())
                .likeCount(0L)
                .createdAt(Instant.ofEpochMilli(saved.getCreatedAt().toEpochMilli()))
                .build();

            userActivityService.addComment(requestDto.getUserId(), commentInfo);
            log.debug("[CommentService] 사용자 활동 내역 댓글 추가 완료 - userId: {}, commentId: {}",
                requestDto.getUserId(), saved.getId());

        } catch (Exception e) {
            log.error("[CommentService] 사용자 활동 내역 댓글 추가 실패 - userId: {}, commentId: {}, error: {}", 
                requestDto.getUserId(), saved.getId(), e.getMessage());
        }

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

        if (comment.isDeleted()) {
            log.warn("[CommentService] 이미 삭제된 댓글입니다 - commentId: {}", commentId);
            return;
        }

        comment.delete();
        commentRepository.save(comment);

        // 기사 댓글 수 감소
        Articles article = articlesRepository.findById(comment.getArticleId())
            .orElseThrow(() -> new RuntimeException("해당 기사를 찾을 수 없습니다. articleId: " + comment.getArticleId()));
        article.decreaseCommentCount();
        articlesRepository.save(article);

        log.debug("[CommentService] 댓글 논리 삭제 완료 - commentId: {}, userId: {}", commentId, userId);

        // 사용자 활동 내역에서 댓글 제거
        try {
            userActivityService.removeComment(userId, commentId);
            log.debug("[CommentService] 사용자 활동 내역 댓글 제거 완료 - userId: {}, commentId: {}", userId, commentId);
        } catch (Exception e) {
            log.error("[CommentService] 사용자 활동 내역 댓글 제거 실패 - userId: {}, commentId: {}, error: {}", 
                userId, commentId, e.getMessage());
        }
    }


    // 물리 삭제
    @Override
    public void deleteCommentPhysically(UUID commentId, UUID userId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new CommentNotFoundException(commentId));

        if (!comment.getUserId().equals(userId)) {
            throw new CommentDeleteException("본인의 댓글만 삭제할 수 있습니다.");
        }

        boolean shouldDecreaseCount = !comment.isDeleted(); // 삭제 안 돼 있었으면 줄인다

        commentRepository.delete(comment);

        // 기사 댓글 수 감소
        if (shouldDecreaseCount) {
            Articles article = articlesRepository.findById(comment.getArticleId())
                .orElseThrow(() -> new RuntimeException("해당 기사를 찾을 수 없습니다. articleId: " + comment.getArticleId()));
            article.decreaseCommentCount();

            log.debug("[CommentService] 댓글 수 감소 (물리 삭제로 인한) - commentId: {}", commentId);
        }

        log.debug("[CommentService] 댓글 물리 삭제 완료 - commentId: {}, userId: {}", commentId, userId);

        // 사용자 활동 내역에서 댓글 제거
        try {
            userActivityService.removeComment(userId, commentId);
            log.debug("[CommentService] 사용자 활동 내역 댓글 제거 완료 - userId: {}, commentId: {}", userId, commentId);
        } catch (Exception e) {
            log.error("[CommentService] 사용자 활동 내역 댓글 제거 실패 - userId: {}, commentId: {}, error: {}", 
                userId, commentId, e.getMessage());
        }
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

        log.debug("[CommentService] 댓글 수정 완료 - commentId: {}, userId: {}", commentId, userId);

        // 사용자 활동 내역에서 댓글 내용 업데이트
        try {
            // 1. 사용자 활동 내역에서 업데이트한 댓글 내용 반영
            userActivityService.updateComment(userId, commentId, request.content());
            log.debug("[CommentService] 사용자 활동 내역 댓글 업데이트 완료 - userId: {}, commentId: {}", userId, commentId);

            // 2. 좋아요를 누른 댓글 목록의 해당 댓글 내용도 업데이트 (모든 사용자)
            userActivityService.updateCommentInLikes(commentId, request.content());
            log.debug("[CommentService] 사용자 활동 내역 댓글 좋아요 항목 업데이트 완료 - commentId: {}", commentId);
        } catch (Exception e) {
            log.error("[CommentService] 사용자 활동 내역 댓글 업데이트 실패 - userId: {}, commentId: {}, error: {}", 
                userId, commentId, e.getMessage());
        }

        return commentMapper.toResponseDto(comment);
    }

    private String encodeCursor(Object orderValue, UUID id) {
        return orderValue + "_" + id.toString();
    }

}