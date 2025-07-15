package org.project.monewping.domain.interest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 관심사 등록 요청 DTO입니다.
 *
 * - name: 관심사 이름(필수, 100자 이하, 영문/숫자/한글/공백만 허용)
 * - keywords: 관심사에 연결할 키워드 문자열 목록(선택)
 */
public record InterestRegisterRequest(
    @NotBlank(message = "관심사 이름은 필수입니다.")
    @Size(max = 100, message = "관심사 이름은 100자를 초과할 수 없습니다.")
    @Pattern(regexp = "^[a-zA-Z0-9가-힣\\s]+$", message = "관심사 이름에는 영문, 숫자, 한글, 공백만 사용할 수 있습니다.")
    String name,
    List<String> keywords
) {} 