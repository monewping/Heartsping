package org.project.monewping.domain.comment.service;

import lombok.RequiredArgsConstructor;
import org.project.monewping.domain.comment.domain.Comment;
import org.project.monewping.domain.comment.dto.CommentResponseDto;
import org.project.monewping.domain.comment.mapper.CommentMapper;
import org.project.monewping.domain.comment.repository.CommentRepository;
import org.project.monewping.global.dto.CursorPageResponse;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 댓글 서비스 구현체
 * 댓글 도메인 비즈니스 로직을 수행한다.
 */
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    /**
     * 댓글 목록 조회 (커서 기반 페이징)
     *
     * @param cursorId 커서 ID
     * @param size 페이지 크기
     * @return 댓글 목록 응답 DTO
     */
    @Override
    public CursorPageResponse<CommentResponseDto> getComments(Long cursorId, int size) {
        List<Comment> comments = commentRepository.findTop10ByIdLessThanOrderByIdDesc(
            cursorId != null ? cursorId : Long.MAX_VALUE
        );

        List<CommentResponseDto> dtoList = comments.stream()
            .map(commentMapper::toResponseDto)
            .toList();

        Long nextIdAfter = dtoList.isEmpty() ? null : dtoList.get(dtoList.size() - 1).getId();

        return new CursorPageResponse<>(
            dtoList,
            nextIdAfter,
            null,
            dtoList.size(),
            dtoList.size(),
            !dtoList.isEmpty()
        );
    }
}