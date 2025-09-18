package com.medinote.medinote_back_khs.insurance.domain.entity;


import com.medinote.medinote_back_khs.common.entity.CreateEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "tbl_prescription")
@EntityListeners(AuditingEntityListener.class)
public class Prescription extends CreateEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, updatable = false)
  private Long visitId;       // 방문 ID (필수, 변경불가)

  @Column(nullable = false, updatable = false)
  private Long memberId;      // 회원 ID (필수, 변경불가)

  private String apiId;

  @Column(nullable = false)
  private LocalDateTime issuedDate;  // 처방일 (필수)

  private String note;        // 비고 (선택)


}
