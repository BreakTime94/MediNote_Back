package com.medinote.medinote_back_kys.common.paging;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.*;

@Getter
@Setter
@ToString
public class Criteria {

    //현재 페이지
    @Min(1)
    private int page = 1;

    //페이지 사이즈
    @Min(1)
    @Max(100)
    private int size = 20;
    
    private List<
            @Pattern(regexp = "^[a-zA-Z0-9_]+,(?i)(asc|desc)$",
                    message = "sort는 'field,asc|desc' 형식이어야 합니다.")
            String> sort = List.of("id,desc");


    /** 검색어(옵션) */
    private String keyword;

    /**
     * 동적 필터(옵션). 필요한 곳에서 꺼내 쓰세요.
     * 예: filters[status]=ACTIVE, filters[categoryId]=2
     */
    private Map<String, String> filters = new HashMap<>();


    /** 페이지 블록 크기 (UI에서 1~10, 11~20 형태) */
    @Min(1)
    private int pageBlockSize = 10;

    /** OFFSET 계산 (JPA 네이티브 쿼리 등에 필요할 때 사용) */
    public int offset() {
        return (page - 1) * size;
    }

    public Sort toSort(Map<String, String> whitelist) {
        if (whitelist == null || whitelist.isEmpty()) {
            // 안전하게 id desc 고정
            return Sort.by(Sort.Order.desc("id"));
        }
        List<Sort.Order> orders = new ArrayList<>();
        for (String token : sort) {
            String[] parts = token.split(",", 2);
            if (parts.length != 2) continue;
            String clientField = parts[0].trim();
            String dir = parts[1].trim().toLowerCase(Locale.ROOT);

            String mapped = whitelist.get(clientField);
            if (mapped == null) {
                // 화이트리스트에 없는 필드는 무시(또는 예외 던져 400 처리도 가능)
                continue;
            }
            Sort.Direction direction = "asc".equals(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;
            orders.add(new Sort.Order(direction, mapped));
        }
        // 안정 정렬을 위해 마지막 타이브레이커로 id 포함 (중복 방지용)
        if (orders.stream().noneMatch(o -> o.getProperty().equals("id"))) {
            orders.add(Sort.Order.desc("id"));
        }
        return orders.isEmpty() ? Sort.by(Sort.Order.desc("id")) : Sort.by(orders);
    }

    /** Pageable 변환 (화이트리스트 필수) */
    public Pageable toPageable(Map<String, String> whitelist) {
        return PageRequest.of(Math.max(page - 1, 0), size, toSort(whitelist));
    }

    /**
     * 페이지 블록/이동 메타 계산
     * @param totalElements 총 레코드 수
     */
    public PageMeta toPageMeta(long totalElements) {
        return PageMeta.of(this, totalElements);
    }
}
