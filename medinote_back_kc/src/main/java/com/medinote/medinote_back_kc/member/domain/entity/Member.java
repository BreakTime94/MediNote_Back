package com.medinote.medinote_back_kc.member.domain.entity;

import com.medinote.medinote_back_kc.member.domain.dto.UpdateRequestDTO;
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

  @Column(nullable = false)
  private String email;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false)
  private String extraEmail;

  @Column(nullable = false)
  private String nickname;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private Role role;

  @Column
  private String profileImagePath;

  @Column
  private String profileMimeType;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private Status status;

  @Column(nullable = false)
  private LocalDateTime regDate;

  @Column
  private LocalDateTime deletedAt;

  public void changeMyPage(UpdateRequestDTO dto) { //프론트에서 변경 없는 값은 기존 값이 등록되게 설정
    this.extraEmail = dto.getExtraEmail();
    this.nickname = dto.getNickname();
    this.profileImagePath = dto.getProfileImagePath();
    this.profileMimeType = dto.getProfileMimeType();
  }

}
