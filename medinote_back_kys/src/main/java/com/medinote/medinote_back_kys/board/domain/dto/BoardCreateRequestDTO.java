package com.medinote.medinote_back_kys.board.domain.dto;

import com.medinote.medinote_back_kys.board.domain.en.PostStatus;
import com.medinote.medinote_back_kys.board.domain.en.QnaStatus;
import lombok.Builder;
import lombok.Getter;
import org.antlr.v4.runtime.misc.NotNull;
import org.springframework.data.annotation.CreatedDate;

@Getter
@Builder
public class BoardUpsertDto {
    private Long memberId;          // 토큰/다른 서비스에서 온 식별자
    private Long boardCategoryId;   // 카테고리 식별자
    private String title;
    private String content;
    private Boolean isPublic;       // null이면 엔티티의 기본값 유지 전략 적용 가능
    private Boolean requireAdminPost;
    private QnaStatus qnaStatus;
    private PostStatus postStatus;
}
