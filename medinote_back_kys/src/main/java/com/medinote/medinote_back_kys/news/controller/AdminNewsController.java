package com.medinote.medinote_back_kys.news.controller;

import com.medinote.medinote_back_kys.common.paging.PageCriteria;
import com.medinote.medinote_back_kys.news.domain.dto.AdminNewsDetailResponseDTO;
import com.medinote.medinote_back_kys.news.domain.dto.AdminNewsListItemResponseDTO;
import com.medinote.medinote_back_kys.news.domain.dto.AdminNewsPublishResponseDTO;
import com.medinote.medinote_back_kys.news.domain.dto.NewsPublishUpdateRequestDTO;
import com.medinote.medinote_back_kys.news.domain.en.ContentType;
import com.medinote.medinote_back_kys.news.service.NewsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/news")
@RequiredArgsConstructor
public class AdminNewsController {

    private final NewsService newsService;

    // 🔹 관리자용 목록
    @GetMapping("/list")
    public Page<AdminNewsListItemResponseDTO> list(
            @ModelAttribute PageCriteria criteria,
            @RequestParam(required = false) ContentType type,
            @RequestParam(required = false) Boolean published,
            @RequestParam(required = false) String keyword
    ) {
        return newsService.getAdminList(criteria, type, published, keyword);
    }

    // 🔹 관리자 상세
    @GetMapping("/{id}")
    public AdminNewsDetailResponseDTO detail(@PathVariable Long id) {
        return newsService.getAdminDetail(id);
    }

    // 🔹 관리자 공개상태 변경 (다건/단건 모두 지원)
    @PatchMapping("/publish")
    public List<AdminNewsPublishResponseDTO> updatePublishStatus(
            @RequestBody @Valid NewsPublishUpdateRequestDTO dto
    ) {
        return newsService.updatePublishStatus(dto.ids(), dto.isPublished());
    }
}
