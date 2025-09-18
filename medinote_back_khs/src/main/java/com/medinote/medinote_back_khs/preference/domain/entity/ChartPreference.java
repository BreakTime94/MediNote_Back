package com.medinote.medinote_back_khs.preference.domain.entity;

import com.medinote.medinote_back_khs.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@EntityListeners(AuditingEntityListener.class)
@Table(name = "tbl_chart_preference")
public class ChartPreference extends BaseEntity {
  // tbl_chart_preference (공용) 차트표시선호도

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long memberId;

  @Column(nullable = false)
  private String targetType;  //적용 대상 (health|insurance)

  @Column(nullable = false)
  private String periodType;  // 기간 유형 (week|month|quarter|year)

}
