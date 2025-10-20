package com.medinote.medinote_back_kc.member.domain.entity.terms;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tbl_terms")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Terms {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  //  정책 코드 (ex: PRIVACY, SERVICE 등)
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PolicyCode policyCode;

  // 버전 (정책 개정 버전)
  @Column(nullable = false)
  private String version;

  //  약관 제목
  @Column(nullable = false)
  private String title;

  // 약관 내용 (text)
  @Column(nullable = false)
  private String content;

  // 발효일
  @Column(nullable = false)
  private LocalDateTime effectiveFrom;

  // 필수 여부
  @Column(nullable = false)
  private Boolean required;

  // 등록일 / 수정일
  @Column(updatable = false)
  private LocalDateTime regDate;

  @Column
  private LocalDateTime modDate;

  //  약관과 약관동의내역(맵핑테이블)간의 관계표시
  @OneToMany(mappedBy = "terms", fetch = FetchType.LAZY)
  private List<MemberTerms> memberTermsList = new ArrayList<>();
}
