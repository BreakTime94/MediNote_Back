package com.medinote.medinote_back_khs.health.domain.entity;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@EntityListeners(AuditingEntityListener.class)
@Table(name = "mlist_medication" )
public class Medication {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String drugCode;  //식약처 코드
  private String nameKo;
  private String ingredient;  //주요성분
  private String form;  //약 제형
  private String company; // 식약처 의약품 정보 API
  private String source;

  @LastModifiedDate
  @Column(name = "mod_date")
  private LocalDateTime modDate;

}
