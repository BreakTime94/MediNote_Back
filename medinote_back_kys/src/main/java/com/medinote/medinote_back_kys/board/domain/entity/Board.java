package com.medinote.medinote_back_kys.board.domain.entity;

import com.medinote.medinote_back_kys.board.domain.dto.BoardCreateRequestDTO;
import com.medinote.medinote_back_kys.board.domain.dto.BoardUpdateRequestDTO;
import com.medinote.medinote_back_kys.board.domain.en.PostStatus;
import com.medinote.medinote_back_kys.board.domain.en.QnaStatus;
import com.medinote.medinote_back_kys.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Table(name = "tbl_board")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert // DB 기본값 사용
public class Board extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="member_id", nullable=false)
    private Long memberId;

    @Column(name="board_category_id", nullable=false)
    private Long boardCategoryId;

    @Column(name="title", nullable=false)
    private String title;

    @Column(name="content", columnDefinition = "TEXT")
    private String content;

    @Column(name="require_admin_post", nullable=false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean requireAdminPost; // 관리자만 볼 수 있는 글 여부

    @Column(name="is_public", nullable=false, columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean isPublic;

    @Enumerated(EnumType.STRING)
    @Column(name="qna_status")
    private QnaStatus qnaStatus;

    @Enumerated(EnumType.STRING)
    @Column(name="post_status")
    private PostStatus postStatus;

    // 생성 전용 생성자 (팩토리에서만 사용)
    private Board(Long memberId, Long boardCategoryId, String title, String content,
                  Boolean requireAdminPost, Boolean isPublic,
                  QnaStatus qnaStatus, PostStatus postStatus) {
        this.memberId = memberId;
        this.boardCategoryId = boardCategoryId;
        this.title = title;
        this.content = content;
        this.requireAdminPost = requireAdminPost;
        this.isPublic = isPublic;
        this.qnaStatus = qnaStatus;
        this.postStatus = postStatus;
    }

    // 생성용 정적 팩토리
    public static Board create(Long memberId, Long categoryId, String title, String content,
                               Boolean requireAdminPost, Boolean isPublic,
                               QnaStatus qnaStatus, PostStatus postStatus) {
        return new Board(memberId, categoryId, title, content,
                requireAdminPost != null ? requireAdminPost : Boolean.FALSE,
                isPublic != null ? isPublic : Boolean.TRUE,
                qnaStatus, postStatus);
    }

    // 의미 있는 변경(업데이트) 메서드들
    public void changeTitle(String title) { this.title = title; }
    public void changeContent(String content) { this.content = content; }
    public void changeVisibility(Boolean isPublic) { this.isPublic = isPublic; }
    public void changeRequireAdminPost(Boolean requireAdminPost) { this.requireAdminPost = requireAdminPost; }
    public void changeQnaStatus(QnaStatus status) { this.qnaStatus = status; }
    public void changePostStatus(PostStatus status) { this.postStatus = status; }

}
