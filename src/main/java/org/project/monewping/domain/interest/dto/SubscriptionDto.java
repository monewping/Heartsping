package org.project.monewping.domain.interest.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

/**
 * 관심사 구독 정보를 응답할 때 사용하는 DTO입니다.
 * <p>구독 ID, 관심사 정보, 구독 생성일 등을 포함합니다.</p>
 *
 * @param id 구독 정보 ID
 * @param interestId 관심사 ID
 * @param interestName 관심사 이름
 * @param interestKeywords 관련 키워드 목록
 * @param interestSubscriberCount 구독자 수
 * @param createdAt 구독한 날짜
 */
@Builder
public record SubscriptionDto(
    UUID id,
    UUID interestId,
    String interestName,
    List<String> interestKeywords,
    Long interestSubscriberCount,
    Instant createdAt
) {}
