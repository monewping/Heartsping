package org.project.monewping.domain.interest.repository.impl;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.project.monewping.domain.interest.dto.InterestDto;
import org.project.monewping.domain.interest.dto.request.CursorPageRequestSearchInterestDto;
import org.project.monewping.domain.interest.dto.response.CursorPageResponseInterestDto;
import org.project.monewping.domain.interest.entity.Interest;
import org.project.monewping.domain.interest.entity.QInterest;
import org.project.monewping.domain.interest.entity.QKeyword;
import org.project.monewping.domain.interest.repository.InterestRepositoryCustom;
import org.project.monewping.domain.interest.repository.SubscriptionRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

/**
 * 관심사 커서 기반 목록 조회를 QueryDSL로 구현하는 Repository입니다.
 *
 * <p>검색어(관심사 이름/키워드), 정렬, 커서 페이지네이션 등
 * 실무에서 요구되는 복잡한 동적 쿼리를 처리합니다.
 * 구독 여부(subscribedByMe)도 함께 반환합니다.</p>
 */
@Repository
@RequiredArgsConstructor
public class InterestRepositoryCustomImpl implements InterestRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final SubscriptionRepository subscriptionRepository;

    /**
     * 커서 기반 관심사 목록을 검색/정렬/페이지네이션하여 반환합니다.
     *
     * <p>검색어가 있으면 관심사 이름 또는 키워드에 부분일치하는 데이터를 조회하고,
     * orderBy/direction에 따라 정렬, 커서(cursor) 이후 데이터만 limit+1개 조회합니다.
     * 구독 여부(subscribedByMe)도 함께 포함됩니다.</p>
     *
     * @param request 검색/정렬/커서/사이즈 등 요청 DTO
     * @param monewRequestUserID 요청자(구독자) ID (구독여부 판단에 활용)
     * @return 커서 페이지네이션 응답 DTO
     * @throws IllegalArgumentException 커서 파싱 실패 등 잘못된 요청일 때
     */
    @Override
    public CursorPageResponseInterestDto searchWithCursor(CursorPageRequestSearchInterestDto request, UUID monewRequestUserID) {
        QInterest interest = QInterest.interest;
        QKeyword keyword = QKeyword.keyword;

        // [검색 조건] 관심사 이름/키워드 부분일치
        BooleanExpression predicate = null;
        if (request.keyword() != null && !request.keyword().isBlank()) {
            predicate = interest.name.containsIgnoreCase(request.keyword())
                .or(interest.keywords.any().name.containsIgnoreCase(request.keyword()));
        }

        // [정렬 조건] orderBy, direction 우선 적용, 동점 방지용 createdAt, id 추가
        OrderSpecifier<?> primaryOrder;
        boolean asc = request.direction() == null || request.direction().equalsIgnoreCase("ASC");
        if (request.orderBy() == null || request.orderBy().equalsIgnoreCase("createdAt")) {
            primaryOrder = asc ? interest.createdAt.asc() : interest.createdAt.desc();
        } else if (request.orderBy().equalsIgnoreCase("name")) {
            primaryOrder = asc ? interest.name.asc() : interest.name.desc();
        } else if (request.orderBy().equalsIgnoreCase("subscriberCount")) {
            primaryOrder = asc ? interest.subscriberCount.asc() : interest.subscriberCount.desc();
        } else {
            primaryOrder = asc ? interest.createdAt.asc() : interest.createdAt.desc();
        }
        // 동점 방지용 보조 정렬: createdAt, id
        OrderSpecifier<?> secondaryOrder = asc ? interest.createdAt.asc() : interest.createdAt.desc();
        OrderSpecifier<?> idOrder = asc ? interest.id.asc() : interest.id.desc();

        // [커서 조건] createdAt < cursorCreatedAt or (createdAt = cursorCreatedAt and id < cursorId) (최신순/createdAt 기준)
        BooleanExpression cursorPredicate = null;
        if (request.cursor() != null && !request.cursor().isBlank() && request.after() != null && !request.after().isBlank()) {
            try {
                Instant cursorCreatedAt = Instant.parse(request.after());
                UUID cursorId = UUID.fromString(request.cursor());
                cursorPredicate = interest.createdAt.lt(cursorCreatedAt)
                    .or(interest.createdAt.eq(cursorCreatedAt).and(interest.id.lt(cursorId)));
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("커서의 생성일 형식이 올바르지 않습니다. ISO-8601 형식(예: 2023-12-01T10:15:30Z)을 사용해주세요.", e);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("커서 ID 형식이 올바르지 않습니다. 유효한 UUID 형식을 사용해주세요.", e);
            }
        }

        BooleanExpression where = predicate;
        if (cursorPredicate != null) {
            where = (where == null) ? cursorPredicate : where.and(cursorPredicate);
        }

        // limit+1: 다음 페이지가 있는지 판별하기 위해 요청한 개수보다 1개 더 조회
        List<Interest> entityList = queryFactory.selectFrom(interest)
                .leftJoin(interest.keywords, keyword).fetchJoin()
                .where(where)
                .orderBy(primaryOrder, secondaryOrder, idOrder)
                .limit(request.limit() + 1)
                .fetch();

        boolean hasNext = entityList.size() > request.limit();
        String nextCursor = hasNext ? entityList.get(request.limit()).getId().toString() : null;
        String nextAfter = hasNext ? entityList.get(request.limit()).getCreatedAt().toString() : null;
        if (hasNext) {
            entityList = entityList.subList(0, request.limit());
        }

        List<UUID> subscribedInterestIds = subscriptionRepository.findInterestIdsByUserId(monewRequestUserID);
        List<InterestDto> content = entityList.stream()
            .map(interestObj -> InterestDto.builder()
                .id(interestObj.getId())
                .name(interestObj.getName())
                .keywords(interestObj.getKeywords().stream().map(k -> k.getName()).toList())
                .subscriberCount(interestObj.getSubscriberCount())
                .subscribedByMe(subscribedInterestIds.contains(interestObj.getId()))
                .build())
            .toList();

        // 전체 개수 (검색 조건 포함, null-safe)
        Long totalElements = queryFactory.select(interest.count())
                .from(interest)
                .leftJoin(interest.keywords, keyword)
                .where(where)
                .fetchOne();
        if (totalElements == null) totalElements = 0L;

        return new CursorPageResponseInterestDto(
            content,
            nextCursor,
            nextAfter,
            request.limit(),
            totalElements,
            hasNext
        );
    }
} 