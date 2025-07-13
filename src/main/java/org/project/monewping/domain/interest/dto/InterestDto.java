package org.project.monewping.domain.interest.dto;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

/**
 * 관심사 정보를 전달하는 DTO입니다.
 *
 * <p>관심사의 식별자, 이름, 키워드, 구독자 수, 구독 여부를 포함합니다.</p>
 */
@Builder
public record InterestDto(
    UUID id,
    String name,
    List<String> keywords,
    Long subscriberCount,
    Boolean subscribedByMe
) {} 