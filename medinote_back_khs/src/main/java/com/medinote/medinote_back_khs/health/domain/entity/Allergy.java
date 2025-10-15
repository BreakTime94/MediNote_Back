package com.medinote.medinote_back_khs.health.domain.entity;

import com.medinote.medinote_back_khs.common.entity.ModiEntity;
import com.medinote.medinote_back_khs.health.domain.enums.AllergyStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@EntityListeners(AuditingEntityListener.class)
@Table(name = "mlist_allergy")
public class Allergy extends ModiEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String code;  //알레르기 코드 (필수, 유니크)

  private String nameEn;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AllergyStatus category;

  @Column(nullable = false)
  private String nameKo;

  private String synonyms;
  private String source;  //출처

}
