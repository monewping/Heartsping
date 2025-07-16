package org.project.monewping.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 사용자 로그인 요청 데이터 전송 객체
 * 
 * <p>
 * 클라이언트로부터 로그인 요청 시 전달되는 정보를 담는 불변 객체입니다.
 * </p>
 * 
 * @param email    사용자 이메일 주소 (필수, 이메일 형식, 최대 100자)
 * @param password 사용자 비밀번호 (필수, 최소 8자, 최대 100자)
 */
public record LoginRequest(
        @NotBlank(message = "이메일은 필수입니다.") @Email(message = "올바른 이메일 형식이어야 합니다.") @Size(max = 100, message = "이메일은 100자 이하여야 합니다.") String email,

        @NotBlank(message = "비밀번호는 필수입니다.") @Size(min = 8, max = 100, message = "비밀번호는 8자 이상 100자 이하여야 합니다.") String password) {
}