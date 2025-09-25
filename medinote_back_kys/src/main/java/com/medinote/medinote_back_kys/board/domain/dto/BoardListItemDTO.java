package com.medinote.medinote_back_kys.board.domain.dto;

import com.medinote.medinote_back_kys.board.domain.en.PostStatus;
import com.medinote.medinote_back_kys.board.domain.en.QnaStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BoardListItemDTO {

    private Long id;
    private Long memberId;
    private Long boardCategoryId;
    private String title;
    private Boolean isPublic;
    private Boolean requireAdminPost;
    private QnaStatus qnaStatus;
    private PostStatus postStatus;
    private LocalDateTime regDate;
    private LocalDateTime modDate;
}
