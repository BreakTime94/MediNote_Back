package com.medinote.medinote_back_kc.member.domain.dto.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class RegisterRequestDTO {
  @Email(message = "올바른 이메일 형식이어야 합니다.")
  @NotBlank(message = "이메일은 필수입니다.")
  private String email;

  @NotBlank(message = "비밀번호는 필수입니다.")
  @Pattern(
          regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#$%^&*])[a-zA-Z0-9!@#$%^&*]{8,16}$",
          message = "비밀번호는 8~16자, 영문/숫자/특수문자 각각 1개 이상 포함해야 합니다."
  )
  private String password;

  @Email(message = "올바른 이메일 형식이어야 합니다.")
  @NotBlank(message = "추가 이메일도 반드시 입력하여주십시오.")
  private String extraEmail;

  @NotBlank(message = "닉네임은 필수입니다.")
  @Pattern(
          regexp = "^[가-힣a-zA-Z0-9]{2,10}$",
          message = "닉네임은 한글, 영문, 숫자만 사용하여 2글자에서 10자까지 가능합니다."
  )
  private String nickname;
}
