package com.medinote.medinote_back_kys.news.controller;

import com.medinote.medinote_back_kys.common.paging.PageCriteria;
import com.medinote.medinote_back_kys.news.domain.dto.NewsPublicListItemResponseDTO;
import com.medinote.medinote_back_kys.news.domain.en.ContentType;
import com.medinote.medinote_back_kys.news.domain.entity.News;
import com.medinote.medinote_back_kys.news.repository.NewsRepository;
import com.medinote.medinote_back_kys.news.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/news") // 최종 URL: /api/news/...
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;
    private final NewsRepository newsRepository;

    // ----------------------------------------------------
    // ✅ 1) 최신 뉴스 (메인 요약)
    // GET /api/news/latest?limit=6
    // ----------------------------------------------------
    @GetMapping("/latest")
    public List<NewsPublicListItemResponseDTO> getLatest(@RequestParam(defaultValue = "6") int limit) {
        return newsService.getLatestNews(limit);
    }

    // ----------------------------------------------------
    // ✅ 2) 전체(타입 미지정) 목록 - 더보기 공용
    // GET /api/news/list?page=1&size=10&sort=pubDate,desc
    // (type 파라미터를 주면 그대로 필터링: NEWS/COLUMN/HEALTH_INFO)
    // ----------------------------------------------------
    @GetMapping("/list")
    public Page<NewsPublicListItemResponseDTO> getList(
            @ModelAttribute PageCriteria criteria,
            @RequestParam(required = false) ContentType type
    ) {
        return newsService.getNewsPage(criteria, type);
    }

    // ----------------------------------------------------
    // ✅ 3) 타입별 전용 목록 (요구사항)
    // ----------------------------------------------------
    // GET /api/news/list/news
    @GetMapping("/list/news")
    public Page<NewsPublicListItemResponseDTO> listNews(@ModelAttribute PageCriteria criteria) {
        return newsService.listNews(criteria);
    }

    // GET /api/news/list/columns
    @GetMapping("/list/columns")
    public Page<NewsPublicListItemResponseDTO> listColumns(@ModelAttribute PageCriteria criteria) {
        return newsService.listColumns(criteria);
    }

    // GET /api/news/list/health-info
    @GetMapping("/list/health-info")
    public Page<NewsPublicListItemResponseDTO> listHealthInfo(@ModelAttribute PageCriteria criteria) {
        return newsService.listHealthInfo(criteria);
    }

    // ----------------------------------------------------
    // ✅ 4) 제목 검색 (옵션) — 타입 필터 지원
    // GET /api/news/search-title?keyword=면역&type=NEWS&page=1&size=10
    // 서비스에서 빈 키워드면 IllegalArgumentException → 400으로 매핑
    // ----------------------------------------------------
    @GetMapping("/search-title")
    public Page<NewsPublicListItemResponseDTO> searchTitle(
            @ModelAttribute PageCriteria criteria,
            @RequestParam(required = false) ContentType type,
            @RequestParam String keyword
    ) {
        if (!StringUtils.hasText(keyword)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "검색 키워드는 비어 있을 수 없습니다.");
        }
        return newsService.searchPublicByTitle(criteria, type, keyword);
    }

    // ----------------------------------------------------
    // ✅ 5) 원문 링크로 안전 리다이렉트(공개글만)
    // GET /api/news/{id}/go  -> 303 See Other + Location: 원문
    // ----------------------------------------------------
    @GetMapping("/{id}/go")
    public ResponseEntity<Void> go(@PathVariable Long id) {
        var news = newsRepository.findById(id)
                .filter(News::getIsPublished)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        String link = news.getLink();
        if (!(link.startsWith("http://") || link.startsWith("https://"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid link");
        }

        return ResponseEntity.status(HttpStatus.SEE_OTHER)
                .location(URI.create(link))
                .build();
    }

    // ----------------------------------------------------
    // ✅ 6) 서비스단 IllegalArgumentException → 400 매핑
    // ----------------------------------------------------
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
