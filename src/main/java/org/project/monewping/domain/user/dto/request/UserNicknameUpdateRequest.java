package org.project.monewping.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserNicknameUpdateRequest(
  @NotBlank(message = "닉네임은 필수입니다.")
  @Size(min = 1, max = 50, message = "닉네임은 1자 이상 50자 이하로 입력해주세요.")
  String nickname
) {} 