package com.medinote.medinote_back_khs.chat.domain.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "tbl_chat_session")
public class ChatSession {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long memberId;

  private LocalDateTime startedDate;
  private LocalDateTime endedDate;
  private LocalDateTime lastMessageDate;

  private String status;  // 세션 상태: bot|waiting|connected|ended

}
