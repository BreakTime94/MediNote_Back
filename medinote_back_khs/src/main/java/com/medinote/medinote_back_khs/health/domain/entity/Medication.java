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

  @Column(nullable = false, unique = true) // 필수
  private String drugCode;     // itemSeq

  @Column(nullable = false)
  private String nameKo;     // itemName

  private String company;    // entpName

  @Column(columnDefinition = "TEXT")
  private String effect;     // efcyQesitm

  @Column(columnDefinition = "TEXT")
  private String useMethod;  // useMethodQesitm

  @Column(columnDefinition = "TEXT")
  private String warning;    // atpnWarnQesitm

  @Column(columnDefinition = "TEXT")
  private String caution;    // atpnQesitm

  @Column(columnDefinition = "TEXT")
  private String interaction;  // intrcQesitm

  @Column(columnDefinition = "TEXT")
  private String sideEffect;   // seQesitm

  @Column(columnDefinition = "TEXT")
  private String storage;      // depositMethodQesitm

  private String openDate;
  private String updateDate;
  private String image;
  private String bizNo;

  private String source;  // API 출처
}
