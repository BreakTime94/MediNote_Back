package com.medinote.medinote_back_khs.chat.domain.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "tbl_agent_assignment")
public class AgentAssignment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long chatSessionId; // 상담 세션 ID
  private Long agentId;  // 상담원 회원 ID


  //비즈니스 로직에 의해 설정됨 -> 관리자 배정 및 상담 종료시 설정
  private LocalDateTime startedDate;
  private LocalDateTime endedDate;

}
