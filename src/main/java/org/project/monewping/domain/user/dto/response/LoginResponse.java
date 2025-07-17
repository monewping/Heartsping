package org.project.monewping.domain.user.dto.response;

import java.time.Instant;
import java.util.UUID;

/**
 * 사용자 로그인 응답 데이터 전송 객체
 * 
 * <p>
 * 로그인 성공 시 클라이언트에게 반환되는 정보를 담는 불변 객체입니다.
 * 보안상 비밀번호는 포함하지 않습니다.
 * </p>
 * 
 * @param id        사용자 고유 식별자 (UUID)
 * @param email     사용자 이메일 주소
 * @param nickname  사용자 닉네임
 * @param createdAt 사용자 생성일시
 */
public record LoginResponse(
    UUID id,
    String email,
    String nickname,
    Instant createdAt) {
} 