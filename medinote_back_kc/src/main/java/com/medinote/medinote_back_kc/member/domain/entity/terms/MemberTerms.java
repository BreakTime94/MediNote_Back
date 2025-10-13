package com.medinote.medinote_back_kc.member.domain.entity.terms;

import com.medinote.medinote_back_kc.member.domain.entity.member.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name="tbl_member_terms")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class MemberTerms {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @ManyToOne
  @JoinColumn(name = "terms_id", nullable = false)
  private Terms terms;

  @Column(nullable = false)
  private Boolean agreed;

  // 동의 일시
  @Column
  @Builder.Default
  private LocalDateTime agreedAt = LocalDateTime.now();

  @PrePersist
  protected void onCreate() {
    if (agreedAt == null) {
      agreedAt = LocalDateTime.now();
    }
  }
}
