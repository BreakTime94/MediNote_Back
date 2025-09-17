package com.medinote.medinote_back_khs.insurance.domain.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@Table(name = "tbl_visit")
public class Visit {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long memberId;

  @Column(nullable = false)
  private Long apiId; // 외부 API 고유 ID

  private LocalDateTime visitDate;  // 방문일시

  private String reason;   // 내원 사유
  private String diagnosis; // 진단명
  private String claimCode; // 보험 청구 코드

  private LocalDateTime regDate;  // 수집일(=API 동기화 시각)
}
