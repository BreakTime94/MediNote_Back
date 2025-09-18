package com.medinote.medinote_back_kys.board.domain.entity;

import com.medinote.medinote_back_kys.board.domain.en.PostStatus;
import com.medinote.medinote_back_kys.board.domain.en.QnaStatus;
import com.medinote.medinote_back_kys.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Table(name = "tbl_board")
@DynamicInsert //DB의 기본값을 그대로 사용
public class Board extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name= "board_category_id", nullable = false)
    private Long boardCategoryId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "is_public", nullable = false, columnDefinition = "tinyint(1)")
    @Builder.Default
    private Boolean isPulbic = true; //0,1 기본값 1

    @Lob
    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "require_admin_post", nullable = false, columnDefinition = "tinyint(1) default 0")
    @Builder.Default
    private Boolean requireAdminPost = false;

    /** enum('WAITING','ANSWERED') */
    @Enumerated(EnumType.STRING)
    @Column(name = "qna_status", nullable = false, length = 16)
    @Builder.Default
    private QnaStatus qnaStatus = QnaStatus.WAITING;

    /** enum('DRAFT','PUBLISHED','HIDDEN','DELETED') */
    @Enumerated(EnumType.STRING)
    @Column(name = "post_status", nullable = false, length = 16)
    @Builder.Default
    private PostStatus postStatus = PostStatus.DRAFT;

}
