package com.medinote.medinote_back_kys.board.domain.dto;

import com.medinote.medinote_back_kys.board.domain.en.PostStatus;
import com.medinote.medinote_back_kys.board.domain.en.QnaStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BoardUpdateRequestDTO {

    @NotNull(message = "게시글 ID는 필수값입니다.")
    private Long id;

    private Long boardCategoryId;

    @NotBlank(message = "제목은 비워둘 수 없습니다.")
    private String title;

    @NotBlank(message = "내용은 비워둘 수 없습니다.")
    private String content;

    private Boolean isPublic;

    private Boolean requireAdminPost;

    private QnaStatus qnaStatus;

    private PostStatus postStatus;
}
