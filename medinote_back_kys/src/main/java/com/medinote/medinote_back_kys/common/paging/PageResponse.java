    package com.medinote.medinote_back_kys.common.paging;

    import lombok.Builder;
    import lombok.Getter;

    import java.util.List;
    import java.util.Map;

    public record PageResponse<T>(
            List<T> content,          // 실제 데이터 목록
            PageMeta page,            // 페이지 메타 정보
            List<String> sorted,      // 클라이언트가 요청한 정렬 조건 echo
            String keyword,           // 검색어 echo (도메인 Cond DTO에서 전달)
            Map<String, String> filters // 필터 조건 echo
    ) {}
