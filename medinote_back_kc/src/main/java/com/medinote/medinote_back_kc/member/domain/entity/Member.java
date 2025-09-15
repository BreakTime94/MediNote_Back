package com.medinote.medinote_back_kc.member.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
  private Status status;

  @Column(nullable = false)
  private LocalDateTime regDate;

  @Column
  private LocalDateTime deletedAt;

  public void updateMember(String extraEmail, String nickname, String profileImagePath, String profileMimeType) {
    this.extraEmail = extraEmail;
    this.nickname = nickname;
    this.profileImagePath = profileImagePath;
    this.profileMimeType = profileMimeType;
  }

  public void updatePassword(String password) {
    this.password = password;
  }

}
