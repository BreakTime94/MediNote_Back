package com.medinote.medinote_back_khs.chat.domain.entity;

import com.medinote.medinote_back_khs.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "tbl_chat_faq")
public class ChatFaq extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String question;  // FAQ 질문 (버튼 텍스트)

  @Column(nullable = false)
  private String answer;  // FAQ 답변
  private String category;  // FAQ 카테고리 (예: 이용안내, 보험, 건강)

  @Column(nullable = false)
  private int sortOrder;  //노출순서

  //regdate, moddate -> BaseEntity
}
