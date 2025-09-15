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
@Table(name = "tbl_chat_message")
public class ChatMessage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long sessionId; // 세션 ID

  @Column(nullable = false)
  private String sender;  //보낸 사람(현재는  user)

  @Column(nullable = false)
  private String content; //메세지 내용
  private Long faqId; // FAQ 참조 (NULL 허용)

  private LocalDateTime regDate;

}
