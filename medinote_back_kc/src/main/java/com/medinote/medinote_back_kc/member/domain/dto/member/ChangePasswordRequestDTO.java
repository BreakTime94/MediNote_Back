package com.medinote.medinote_back_kc.member.domain.dto.member;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequestDTO {
  @NotBlank(message = "비밀번호는 필수입니다.")
  @Pattern(
          regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#$%^&*])[a-zA-Z0-9!@#$%^&*]{8,16}$",
          message = "비밀번호는 8~16자, 영문/숫자/특수문자 각각 1개 이상 포함해야 합니다."
  )
  private String newPassword;
}
