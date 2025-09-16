package com.medinote.medinote_back_khs.preference.domain.entity;

import com.medinote.medinote_back_khs.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@Table(name = "tbl_chart_preference")
public class ChartPreference extends BaseEntity {
  // tbl_chart_preference (공용)

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long memberId;

  private String targetType;  //적용 대상 (health|insurance)
  private String periodType;  // 기간 유형 (week|month|quarter|year)

}
