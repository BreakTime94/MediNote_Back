package com.medinote.medinote_back_khs.chat.domain.entity;

import com.medinote.medinote_back_khs.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tbl_chat_faq")
public class ChatFaq extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String question;
  private String answer;
  private String category;
  private int sortOrder;  //노출순서

  //regdate, moddate -> BaseEntity
}
