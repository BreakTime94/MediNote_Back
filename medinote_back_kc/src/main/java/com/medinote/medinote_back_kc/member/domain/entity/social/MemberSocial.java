package com.medinote.medinote_back_kc.member.domain.entity.social;

import com.medinote.medinote_back_kc.member.domain.entity.member.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_member_social")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberSocial {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Provider provider; // google, kakao, naver ...

  @Column(nullable = false)
  private String providerUserId; // 소셜 서비스에서 제공한 고유 ID

  @Column(nullable = false)
  private String email; // 소셜에서 가져온 이메일

  @Column
  private String profileImageUrl;

  @Column
  private String nickname;

  @Column
  @Builder.Default
  private LocalDateTime connectedAt = null;

  private LocalDateTime disconnectedAt;

  @Column(nullable = false)
  private String rawProfileJson; // 소셜 API 응답 원본 JSON

  @PrePersist
  protected void onCreate() {
    if (connectedAt == null) {
      connectedAt = LocalDateTime.now();
    }
  }

}

