package com.medinote.medinote_back_kys.common.paging;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class PageResponse<T> {

    private final List<T> content;
    private final PageMeta page;
    private final List<String> sorted;           // 받은 sort 토큰 echo
    private final String keyword;                // 검색어 echo
    private final Map<String, String> filters;   // 필터 echo
}
