package com.medinote.medinote_back_kc.member.domain.entity;

import com.medinote.medinote_back_kc.member.domain.dto.member.UpdateRequestDTO;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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

  @Column
  private String extraEmail;

  @Column(nullable = false)
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
  @Builder.Default
  private boolean fromSocial = false;

  @Column
  @Builder.Default
  private LocalDateTime regDate = LocalDateTime.now();

  @Column
  private LocalDateTime deletedAt;

  public void changeMyPage(UpdateRequestDTO dto) { //프론트에서 변경 없는 값은 기존 값이 등록되게 설정
    this.extraEmail = dto.getExtraEmail();
    this.nickname = dto.getNickname();
    this.profileImagePath = dto.getProfileImagePath();
    this.profileMimeType = dto.getProfileMimeType();
  }

}
