package com.medinote.medinote_back_khs.health.domain.entity;


import com.medinote.medinote_back_khs.common.entity.ModiEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@EntityListeners(AuditingEntityListener.class)
@Table(name = "mlist_medication" )
public class Medication extends ModiEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String drugCode;    // 식약처 코드 (필수, 유니크)

  @Column(nullable = false)
  private String nameKo;      // 한글명 (필수)

  @Column(nullable = false)
  private String ingredient;  // 주요성분 (필수)

  private String form;        // 약 제형 (선택)
  private String company;     // 제약회사 (선택)
  private String source;      // 식약처 의약품 정보 API (선택)

}
