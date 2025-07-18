package org.project.monewping.domain.interest.dto;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

/**
 * 관심사 정보 DTO입니다.
 *
 * @param id 관심사 ID
 * @param name 관심사 이름
 * @param keywords 관련 키워드 목록
 * @param subscriberCount 구독자 수
 * @param subscribedByMe 요청자의 구독 여부
 */
@Builder
public record InterestDto(
    UUID id,
    String name,
    List<String> keywords,
    Long subscriberCount,
    Boolean subscribedByMe
) {} 