package com.medinote.medinote_back_kys.news.domain.entity;

import com.medinote.medinote_back_kys.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "news_article")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class News extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 언론사 이름
    @Column(nullable = false, length = 255)
    private String sourceName;

    // RSS 피드 URL
    @Column(nullable = false, length = 500)
    private String feedUrl;

    // 기사 제목
    @Column(nullable = false, length = 500)
    private String title;

    // 기사 원문 링크 (중복 방지)
    @Column(nullable = false, unique = true, length = 500)
    private String link;

    // 기사 발행일
    private java.time.LocalDateTime pubDate;

    // 작성자 / 기자명
    @Column(length = 255)
    private String author;

    // 카테고리 코드
    @Column(length = 50)
    private String sectionCode;

    @Column(length = 50)
    private String subSectionCode;

    @Column(length = 50)
    private String serialCode;

    // 기사 요약 or 본문 일부
    @Column(columnDefinition = "TEXT")
    private String description;

    // 대표 이미지 (필요 시)
    @Column(length = 500)
    private String image;

    // 관리자 승인 여부
    @Builder.Default
    @Column(nullable = false)
    private Boolean isPublished = false;

    // ===== 편의 메서드 =====
    public void approve() {
        this.isPublished = true;
    }

    public void reject() {
        this.isPublished = false;
    }
}
