package com.medinote.medinote_back_kys.reply.domain.entity;

import com.medinote.medinote_back_kys.common.entity.BaseEntity;
import com.medinote.medinote_back_kys.reply.domain.en.ReplyTargetType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Table(name = "tbl_reply")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@DynamicInsert
public class Reply extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 폴리모픽 대상 타입: "BOARD", "NEWS" 등 */
    @Enumerated(EnumType.STRING)
    @Column(name = "link_type", nullable = false, length = 50)
    private ReplyTargetType linkType;

    /** 폴리모픽 대상 PK: Board.id 또는 News.id */
    @Column(name = "link_id", nullable = false)
    private Long linkId;

    /** 작성자 회원 PK */
    @Column(name = "member_id", nullable = false)
    private Long memberId;

    /** (선택) 제목을 쓰는 경우에만 사용 */
    @Column(name = "title")
    private String title;

    /** 본문 */
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /** 공개 여부 (DB 기본값 1) */
    @Column(name = "is_public", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean isPublic;

    /* ===== 편의 팩토리 ===== */
    public static Reply forBoard(Long boardId, Long memberId, String content, Boolean isPublic) {
        return Reply.builder()
                .linkType(ReplyTargetType.BOARD)
                .linkId(boardId)
                .memberId(memberId)
                .content(content)
                .isPublic(isPublic != null ? isPublic : Boolean.TRUE)
                .build();
    }

    public static Reply forNews(Long newsId, Long memberId, String content, Boolean isPublic) {
        return Reply.builder()
                .linkType(ReplyTargetType.NEWS)
                .linkId(newsId)
                .memberId(memberId)
                .content(content)
                .isPublic(isPublic != null ? isPublic : Boolean.TRUE)
                .build();
    }

    /* ===== 의미 있는 변경 메서드 ===== */
    public void changeContent(String content) { this.content = content; }
    public void changeVisibility(Boolean isPublic) { this.isPublic = isPublic; }
    public void changeTitle(String title) { this.title = title; }
}
