package com.medinote.medinote_back_kys.board.domain.dto;

import com.medinote.medinote_back_kys.board.domain.en.PostStatus;
import com.medinote.medinote_back_kys.board.domain.en.QnaStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BoardDetailResponseDTO {
    private Long id;
    private Long memberId;
    private Long boardCategoryId;
    private String title;
    private String content;
    private Boolean isPublic;
    private Boolean requireAdminPost;
    private QnaStatus qnaStatus;
    private PostStatus postStatus;
    private LocalDateTime regDate;
    private LocalDateTime modDate;
}
