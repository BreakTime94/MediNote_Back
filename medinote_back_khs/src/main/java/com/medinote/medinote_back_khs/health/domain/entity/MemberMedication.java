package com.medinote.medinote_back_khs.health.domain.entity;

import com.medinote.medinote_back_khs.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;


@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@EntityListeners(AuditingEntityListener.class)
@Table(name = "tbl_member_medication")
public class MemberMedication extends BaseEntity {
  //복용중일 때 추가 정보(복용량, 기간, 현재 복용여부 등)

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long memberId;

  @Column(nullable = false)
  private Long medicationId;

  private String dosage;  //복용량

  private LocalDate startDate;
  private LocalDate endDate;

  @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
  private Boolean isCurrent = true; //현재 복용 여부

  //regdate, modate -> BaseEntity 상속
}
