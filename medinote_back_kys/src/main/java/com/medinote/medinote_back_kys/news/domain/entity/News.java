package com.medinote.medinote_back_kys.news.domain.entity;

import com.medinote.medinote_back_kys.common.entity.BaseEntity;
import com.medinote.medinote_back_kys.news.domain.en.ContentType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "news_article")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(toBuilder = true)
public class News extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** DB 생성 컬럼: section_code 등으로 계산됨 (읽기 전용) */
    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false)
    private ContentType contentType;

    /** 언론사 이름 */
    @Column(name = "source_name", nullable = false, length = 255)
    private String sourceName;

    /** 채널 제목(예: 헬스경향 - 칼럼) */
    @Column(name = "channel_title", length = 255)
    private String channelTitle;

    /** RSS 피드 URL */
    @Column(name = "feed_url", nullable = false, length = 500)
    private String feedUrl;

    /** 기사 제목 */
    @Column(nullable = false, length = 500)
    private String title;

    /** 기사 원문 링크 (UNIQUE) */
    @Column(nullable = false, unique = true, length = 500)
    private String link;

    /** 기사 발행일 */
    @Column(name = "pub_date")
    private LocalDateTime pubDate;

    /** 작성자 / 기자명 */
    @Column(length = 255)
    private String author;

    /** 카테고리 코드들 */
    @Column(name = "section_code", length = 50)
    private String sectionCode;

    @Column(name = "sub_section_code", length = 50)
    private String subSectionCode;

    @Column(name = "serial_code", length = 50)
    private String serialCode;

    /** 기사 요약/본문 일부 */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** 대표 이미지 URL */
    @Column(length = 500)
    private String image;

    /** 관리자 승인 여부 */
    @Builder.Default
    @Column(name = "is_published", nullable = false)
    private Boolean isPublished = false;

    // ===== 편의 메서드 =====
    public void approve() { this.isPublished = true; }
    public void reject() { this.isPublished = false; }

    // 선택적 판별 메서드
    public boolean isNews() { return this.contentType == ContentType.NEWS; }
    public boolean isColumn() { return this.contentType == ContentType.COLUMN; }
    public boolean isHealthInfo() { return this.contentType == ContentType.HEALTH_INFO; }
}
