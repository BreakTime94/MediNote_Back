package com.medinote.medinote_back_khs.health.domain.entity;

import com.medinote.medinote_back_khs.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;


@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Table(name = "tbl_member_medication")
public class MemberMedication extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long memberId;

  @Column(nullable = false)
  private Long medicationId;

  private LocalDate startDate;
  private LocalDate endDate;

  @Column(nullable = false)
  private Boolean isCurrent = true; //현재 복용 여부

  //regdate, modate -> BaseEntity 상속
}
