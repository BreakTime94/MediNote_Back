package com.medinote.medinote_back_kys.board.domain.dto;

import com.medinote.medinote_back_kys.board.domain.en.QnaStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BoardSearchCond {

    //조회 조건을 담당하는 DTO
    private Long categoryId;
    private String keyword; //검색시
    private QnaStatus qnaStatus;
    private LocalDateTime from;
    private LocalDateTime to;

    private Long writerId; //작성자 필터
}
