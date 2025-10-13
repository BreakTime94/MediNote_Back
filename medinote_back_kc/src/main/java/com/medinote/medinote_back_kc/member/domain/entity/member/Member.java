package com.medinote.medinote_back_kc.member.domain.entity.member;

import com.medinote.medinote_back_kc.member.domain.dto.member.ChangePasswordRequestDTO;
import com.medinote.medinote_back_kc.member.domain.dto.member.UpdateRequestDTO;
import com.medinote.medinote_back_kc.member.domain.entity.social.MemberSocial;
import com.medinote.medinote_back_kc.member.domain.entity.terms.MemberTerms;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tbl_member")
public class Member {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String email;

  @Column
  private String password;

  @Column(nullable = false, unique = true)
  private String extraEmail;

  @Column(nullable = false, unique = true)
  private String nickname;

  @Enumerated(EnumType.STRING)
  @Builder.Default()
  private Role role = Role.USER;

  @Column
  private String profileImagePath;

  @Column
  private String profileMimeType;

  @Enumerated(EnumType.STRING)
  @Builder.Default
  private Status status = Status.ACTIVE;

  @Column(nullable = false)
  private boolean fromSocial;

  @Column
  @Builder.Default
  private LocalDateTime regDate = null;

  @Column
  private LocalDateTime deletedAt;

  @Column(nullable = false)
  private boolean extraEmailVerified;

  // 소셜 로그인 계정들
  @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
  @Builder.Default
  private List<MemberSocial> socialAccounts = new ArrayList<>();

  //회원가입시 동의한 약관들
  @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
  @Builder.Default
  private List<MemberTerms> memberTerms = new ArrayList<>();

  public void changePassword(String password) {
    this.password = password;
  }

  public void changeMyPage(UpdateRequestDTO dto) { //프론트에서 변경 없는 값은 기존 값이 등록되게 설정
    this.extraEmail = dto.getExtraEmail();
    this.nickname = dto.getNickname();
    this.profileImagePath = dto.getProfileImagePath();
    this.profileMimeType = dto.getProfileMimeType();
    this.extraEmailVerified = dto.isExtraEmailVerified();
  }

  @PrePersist
  protected void onCreate() {
    if (regDate == null) {
      regDate = LocalDateTime.now();
    }
  }

}
