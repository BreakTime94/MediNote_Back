package com.medinote.medinote_back_khs.insurance.domain.entity;


import com.medinote.medinote_back_khs.common.entity.CreateEntity;
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
public class Visit extends CreateEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long memberId;

  @Column(nullable = false)
  private String apiId; // 외부 API 고유 ID(공공데이터/보험청구 API 연동)

  private LocalDateTime visitDate;  // 방문일시

  private String reason;   // 내원 사유
  private String diagnosis; // 진단명
  private String claimCode; // 보험 청구 코드

}
