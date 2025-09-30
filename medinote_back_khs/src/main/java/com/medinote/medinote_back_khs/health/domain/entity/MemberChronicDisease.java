package com.medinote.medinote_back_khs.health.domain.entity;


import com.medinote.medinote_back_khs.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@EntityListeners(AuditingEntityListener.class)
@Table(name = "tbl_member_chronic_disease")
public class MemberChronicDisease extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long memberId;

  @Column(nullable = false)
  private Long chronicDiseaseId;  //기저질환 FK

  //regdate, modate -> BaseEntity 상속
}
