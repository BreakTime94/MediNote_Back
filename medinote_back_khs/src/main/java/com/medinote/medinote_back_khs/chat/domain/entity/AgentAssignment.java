package com.medinote.medinote_back_khs.chat.domain.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tbl_agent_assignment")
public class AgentAssignment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long chatSessionId; // 상담 세션 ID
  private Long agentId;  // 상담원 회원 ID

  private LocalDateTime startedDate;
  private LocalDateTime endedDate;

}
