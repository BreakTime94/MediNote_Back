package com.medinote.medinote_back_kc.member.domain.dto.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class UpdateRequestDTO {
  @Email(message = "올바른 이메일 형식이어야 합니다.")
  @NotBlank(message = "이메일은 필수입니다.")
  private String extraEmail;

  @NotBlank(message = "닉네임은 필수입니다.")
  @Pattern(
          regexp = "^[가-힣a-zA-Z0-9]{2,16}$",
          message = "닉네임은 한글, 영문, 숫자만 사용하여 2글자에서 16자까지 가능합니다.")
  private String nickname;

  private String profileImagePath;

  private String profileMimeType;

  private boolean extraEmailVerified;
}
