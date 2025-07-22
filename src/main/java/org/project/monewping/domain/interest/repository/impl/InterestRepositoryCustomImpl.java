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
import org.project.monewping.domain.notification.exception.InvalidCursorFormatException;
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
        BooleanExpression predicate = buildSearchPredicate(request, interest);

        // [정렬 조건] orderBy, direction 우선 적용, 동점 방지용 createdAt, id 추가
        OrderSpecifier<?> primaryOrder = buildPrimaryOrder(request, interest);
        boolean asc = request.direction() == null || request.direction().equalsIgnoreCase("ASC");
        OrderSpecifier<?> secondaryOrder = asc ? interest.createdAt.asc() : interest.createdAt.desc();
        OrderSpecifier<?> idOrder = asc ? interest.id.asc() : interest.id.desc();

        // [커서 조건] createdAt < cursorCreatedAt or (createdAt = cursorCreatedAt and id < cursorId) (최신순/createdAt 기준)
        BooleanExpression cursorPredicate = buildCursorPredicate(request, interest);

        BooleanExpression where = predicate;
        if (cursorPredicate != null) {
            where = (where == null) ? cursorPredicate : where.and(cursorPredicate);
        }

        // QueryDSL의 JPAQueryFactory를 사용하여 동적 쿼리를 생성합니다.
        // - selectFrom(interest): Interest 엔티티를 기준으로 조회
        // - leftJoin(interest.keywords, keyword).fetchJoin(): Interest와 연관된 Keyword를 LEFT JOIN + 즉시 로딩(fetch join)으로 가져옴
        // - where(where): 검색/커서 조건을 모두 적용
        // - orderBy(...): 정렬 조건(주/보조/ID) 적용
        // - limit(request.limit() + 1): 페이지네이션을 위해 요청 개수보다 1개 더 조회(다음 페이지 존재 여부 판별용)
        // - fetch(): 쿼리 실행 및 결과 리스트 반환
        List<Interest> entityList = queryFactory.selectFrom(interest)
                .leftJoin(interest.keywords, keyword).fetchJoin()
                .where(where)
                .orderBy(primaryOrder, secondaryOrder, idOrder)
                .limit(request.limit() + 1)
                .fetch();

        // entityList에는 limit+1개가 들어있음(다음 페이지 존재 여부 확인용)
        // - hasNext: 다음 페이지가 있는지 여부
        // - nextCursor/nextAfter: 다음 페이지 요청을 위한 커서 값(마지막 요소의 id/createdAt)
        // - 실제 반환할 데이터는 limit개로 잘라서 반환
        boolean hasNext = entityList.size() > request.limit();
        String nextCursor = hasNext ? entityList.get(request.limit()).getId().toString() : null;
        String nextAfter = hasNext ? entityList.get(request.limit()).getCreatedAt().toString() : null;
        if (hasNext) {
            // entityList를 limit개로 잘라서 반환(마지막 1개는 커서 계산용)
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

    /**
     * 검색어(관심사 이름/키워드)에 대한 부분일치 조건을 생성합니다.
     *
     * @param request 검색 요청 DTO
     * @param interest QInterest 엔티티
     * @return 검색 조건(BooleanExpression), 없으면 null
     */
    private BooleanExpression buildSearchPredicate(CursorPageRequestSearchInterestDto request, QInterest interest) {
        if (request.keyword() != null && !request.keyword().isBlank()) {
            return interest.name.containsIgnoreCase(request.keyword())
                .or(interest.keywords.any().name.containsIgnoreCase(request.keyword()));
        }
        return null;
    }

    /**
     * 정렬 기준(orderBy, direction)에 따라 QueryDSL OrderSpecifier를 생성합니다.
     *
     * @param request 정렬 요청 DTO
     * @param interest QInterest 엔티티
     * @return 정렬 조건(OrderSpecifier)
     */
    private OrderSpecifier<?> buildPrimaryOrder(CursorPageRequestSearchInterestDto request, QInterest interest) {
        boolean asc = request.direction() == null || request.direction().equalsIgnoreCase("ASC");
        if (request.orderBy() == null || request.orderBy().equalsIgnoreCase("createdAt")) {
            return asc ? interest.createdAt.asc() : interest.createdAt.desc();
        } else if (request.orderBy().equalsIgnoreCase("name")) {
            return asc ? interest.name.asc() : interest.name.desc();
        } else if (request.orderBy().equalsIgnoreCase("subscriberCount")) {
            return asc ? interest.subscriberCount.asc() : interest.subscriberCount.desc();
        } else {
            return asc ? interest.createdAt.asc() : interest.createdAt.desc();
        }
    }

    /**
     * 커서 기반 페이지네이션 조건을 생성합니다.
     *
     * @param request 커서/after 정보가 담긴 요청 DTO
     * @param interest QInterest 엔티티
     * @return 커서 조건(BooleanExpression), 없으면 null
     * @throws IllegalArgumentException 커서 파싱 실패 시
     */
    private BooleanExpression buildCursorPredicate(CursorPageRequestSearchInterestDto request, QInterest interest) {
        if (request.cursor() != null && !request.cursor().isBlank() && request.after() != null && !request.after().isBlank()) {
            try {
                Instant cursorCreatedAt = Instant.parse(request.after());
                UUID cursorId = UUID.fromString(request.cursor());
                return interest.createdAt.lt(cursorCreatedAt)
                    .or(interest.createdAt.eq(cursorCreatedAt).and(interest.id.lt(cursorId)));
            } catch (DateTimeParseException e) {
                throw new InvalidCursorFormatException("커서의 생성일 형식이 올바르지 않습니다. ISO-8601 형식(예: 2023-12-01T10:15:30Z)을 사용해주세요.", e);
            } catch (IllegalArgumentException e) {
                throw new InvalidCursorFormatException("커서 ID 형식이 올바르지 않습니다. 유효한 UUID 형식을 사용해주세요.", e);
            }
        }
        return null;
    }
} 