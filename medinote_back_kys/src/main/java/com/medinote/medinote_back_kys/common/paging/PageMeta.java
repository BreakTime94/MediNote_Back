package com.medinote.medinote_back_kys.common.paging;

public record PageMeta(
        int page,          // 현재 페이지 번호
        int size,          // 페이지당 요소 개수
        long totalElements,// 전체 요소 개수
        int totalPages,    // 전체 페이지 수

        // 페이지 이동 관련 정보
        int firstPage,     // 항상 1
        int lastPage,      // totalPages
        Integer prevPage,  // null 이거나 (page-1)
        Integer nextPage,  // null 이거나 (page+1)

        // 블록 단위 페이지 정보 (예: 1~10, 11~20 ...)
        int pageBlockSize, // 블록 크기 (보통 10)
        int startPage,     // 블록 시작 페이지 번호
        int endPage,       // 블록 끝 페이지 번호 (마지막 페이지 이상으로 넘어가지 않음)
        boolean hasPrevBlock, // 이전 블록 존재 여부
        boolean hasNextBlock  // 다음 블록 존재 여부
) {

    /**
     * PageCriteria와 totalElements(총 데이터 수)를 기반으로 PageMeta를 생성합니다.
     * @param c              클라이언트에서 전달된 페이지 요청 조건
     * @param totalElements  전체 데이터 개수
     * @return PageMeta 불변 객체
     */
    public static PageMeta of(PageCriteria c, long totalElements) {
        int page = Math.max(c.getPage(), 1);     // 현재 페이지 (최소 1)
        int size = Math.max(c.getSize(), 1);     // 페이지 사이즈 (최소 1)

        // 전체 페이지 수 계산 (올림 처리)
        int totalPages = (int) Math.max(1,
                (totalElements + size - 1) / size);

        int firstPage = 1;
        int lastPage  = totalPages;

        // 이전/다음 페이지 계산
        Integer prevPage = (page > firstPage) ? page - 1 : null;
        Integer nextPage = (page < lastPage)  ? page + 1 : null;

        // 블록 단위 계산 (예: blockSize=10 → 1~10, 11~20)
        int blockSize = Math.max(c.getPageBlockSize(), 1);
        int blockIdx  = (page - 1) / blockSize;
        int startPage = blockIdx * blockSize + 1;
        int endPage   = Math.min(startPage + blockSize - 1, totalPages);

        boolean hasPrevBlock = startPage > 1;
        boolean hasNextBlock = endPage < totalPages;

        // record는 new 키워드로 생성
        return new PageMeta(
                page, size, totalElements, totalPages,
                firstPage, lastPage, prevPage, nextPage,
                blockSize, startPage, endPage,
                hasPrevBlock, hasNextBlock
        );
    }
}