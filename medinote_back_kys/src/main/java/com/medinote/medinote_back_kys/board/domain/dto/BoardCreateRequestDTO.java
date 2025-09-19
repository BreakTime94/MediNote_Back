package com.medinote.medinote_back_kys.board.domain.dto;

import com.medinote.medinote_back_kys.board.domain.en.PostStatus;
import com.medinote.medinote_back_kys.board.domain.en.QnaStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class BoardCreateRequestDTO {

    @NotNull(message = "회원 ID는 필수값입니다.")
    private Long memberId;

    @NotNull(message = "카테고리 ID는 필수값입니다.")
    private Long boardCategoryId;

    @NotBlank(message = "제목은 비워둘 수 없습니다.")
    private String title;

    @NotBlank(message = "내용은 비워둘 수 없습니다.")
    private String content;

    /** 기본값 true (서비스/엔티티에서 처리) */
    private Boolean isPublic;

    /** 관리자 승인 필요 여부 (기본값 false) */
    private Boolean requireAdminPost;

    /** QnA 상태 (기본값 WAITING) */
    private QnaStatus qnaStatus;

    /** 게시글 상태 (기본값 DRAFT) */
    private PostStatus postStatus;
}
