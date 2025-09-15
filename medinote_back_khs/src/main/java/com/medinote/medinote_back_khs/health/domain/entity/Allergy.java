package com.medinote.medinote_back_khs.health.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.mapstruct.Mapper;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@EntityListeners(AuditingEntityListener.class)
@Table(name = "mlist_allergy")
public class Allergy {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String code;
  private String nameEn;
  private String nameKo;
  private String synonyms;
  private String source;  //출처

  @LastModifiedDate
  @Column(name = "mod_date")
  private LocalDateTime modDate;
}
