package com.medinote.medinote_back_kys.common.paging;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PageMeta {

    private final int page; //현재 페이지
    private final int size; // 페이지당 개수
    private final long totalElements; //전체 요소 개수
    private final int totalPages; //전체 페이지 수

    //페이지 이동용
    private final int firstPage;     // 1
    private final int lastPage;      // totalPages
    private final Integer prevPage;  // null or page-1
    private final Integer nextPage;  // null or page+1

    // 블록(예: 10개 단위) 정보
    private final int pageBlockSize; // 보통 10
    private final int startPage;     // 블록 시작(1, 11, 21,…)
    private final int endPage;       // 블록 끝(10, 20, 30, … 또는 lastPage)
    private final boolean hasPrevBlock;
    private final boolean hasNextBlock;

    public static PageMeta of(Criteria c, long totalElements) {
        int page = Math.max(c.getPage(), 1);
        int size = Math.max(c.getSize(), 1);

        int totalPages = (int) Math.max(1,
                (totalElements + size - 1) / size);

        int firstPage = 1;
        int lastPage  = totalPages;

        Integer prevPage = (page > firstPage) ? page - 1 : null;
        Integer nextPage = (page < lastPage)  ? page + 1 : null;

        int blockSize = Math.max(c.getPageBlockSize(), 1);
        int blockIdx  = (page - 1) / blockSize;
        int startPage = blockIdx * blockSize + 1;
        int endPage   = Math.min(startPage + blockSize - 1, totalPages);

        boolean hasPrevBlock = startPage > 1;
        boolean hasNextBlock = endPage < totalPages;

        return PageMeta.builder()
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .firstPage(firstPage)
                .lastPage(lastPage)
                .prevPage(prevPage)
                .nextPage(nextPage)
                .pageBlockSize(blockSize)
                .startPage(startPage)
                .endPage(endPage)
                .hasPrevBlock(hasPrevBlock)
                .hasNextBlock(hasNextBlock)
                .build();
    }
}
