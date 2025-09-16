package com.medinote.medinote_back_khs.chat.domain.entity;


import com.medinote.medinote_back_khs.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor
@Table(name = "tbl_chat_intent")
public class ChatIntent extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String keyword; //키워드
  private Long faqId; // 매핑된 FAQ ID

}
