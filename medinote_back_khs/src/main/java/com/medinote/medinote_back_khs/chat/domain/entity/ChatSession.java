package com.medinote.medinote_back_khs.chat.domain.entity;


import com.medinote.medinote_back_khs.chat.domain.en.ChatSessionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
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


  // 세션 상태: bot|waiting|connected|ended
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ChatSessionStatus status;

}
