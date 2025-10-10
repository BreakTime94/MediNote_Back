package com.medinote.medinote_back_kys.board.domain.dto;

import com.medinote.medinote_back_kys.common.paging.PageCriteria;

public record BoardListRequest (

    BoardSearchCond cond,   // record: categoryId/keyword/qnaStatus/from/to/writerId
    PageCriteria criteria   // class: page/size/sort/filters/pageBlockSize
) {}
