package com.medinote.medinote_back_khs.health.domain.entity;


import com.medinote.medinote_back_khs.common.entity.ModiEntity;
import com.medinote.medinote_back_khs.health.domain.en.ChronicDiseaseStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@EntityListeners(AuditingEntityListener.class)
@Table(name = "mlist_chronic_disease" )
public class ChronicDisease extends ModiEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String code;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ChronicDiseaseStatus category;

  private String nameEn;

  @Column(nullable = false)
  private String nameKo;

  private String synonyms;  // 동의어/별칭
  private String source;   // 데이터 출처 (WHO ICD-11 등)

}
