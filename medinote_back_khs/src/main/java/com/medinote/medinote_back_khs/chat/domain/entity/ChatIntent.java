package com.medinote.medinote_back_khs.chat.domain.entity;


import com.medinote.medinote_back_khs.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter @Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "tbl_chat_intent")
public class ChatIntent extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  //키워드와 FAQ를 매핑하는 테이블 -> 필수값

  @Column(nullable = false)
  private String keyword; //키워드

  @Column(nullable = false)
  private Long faqId; // 매핑된 FAQ ID

}
