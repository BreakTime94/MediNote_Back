package com.medinote.medinote_back_khs.health.domain.entity;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@EntityListeners(AuditingEntityListener.class)
@Table(name = "mlist_chronic_disease" )
public class ChronicDisease  {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String code;

  private String nameEn;

  @Column(nullable = false)
  private String nameKo;

  private String synonyms;  // 동의어/별칭
  private String source;   // 데이터 출처 (WHO ICD-11 등)

  @LastModifiedDate
  @Column(name = "mod_date")
  private LocalDateTime modDate;


}
