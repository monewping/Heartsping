package org.project.monewping.domain.interest.dto;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

/**
 * 관심사 정보 DTO입니다.
 *
 * - id: 관심사 식별자(UUID)
 * - name: 관심사 이름
 * - keywords: 관심사에 연결된 키워드 문자열 목록
 * - subscriberCount: 구독자 수
 * - subscribedByMe: 내가 구독 중인지 여부
 */
@Builder
public record InterestDto(
    UUID id,
    String name,
    List<String> keywords,
    Long subscriberCount,
    Boolean subscribedByMe
) {} 