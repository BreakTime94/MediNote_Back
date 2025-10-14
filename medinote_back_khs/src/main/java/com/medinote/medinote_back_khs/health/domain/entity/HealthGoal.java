package com.medinote.medinote_back_khs.health.domain.entity;

import com.medinote.medinote_back_khs.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity  // 이게 없었어요!
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter  // 이것도 필요해요!
@EntityListeners(AuditingEntityListener.class)
@Table(name = "tbl_health_goal")
public class HealthGoal extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "member_id", nullable = false, unique = true)
  private Long memberId;

  @Column(nullable = false)
  private Double targetWeight;  // 목표 체중 (kg)

}