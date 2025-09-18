package com.medinote.medinote_back_khs.insurance.domain.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "tbl_prescription")
@EntityListeners(AuditingEntityListener.class)
public class Prescription {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)

  private Long id;

  @Column(updatable = false)
  private Long visitId;
  @Column(updatable = false)
  private Long memberId;

  private String apiId;

  private LocalDateTime issuedDate;
  private String note;  //비고

  @CreatedDate
  @Column(updatable = false)
  private LocalDateTime regDate;


}
