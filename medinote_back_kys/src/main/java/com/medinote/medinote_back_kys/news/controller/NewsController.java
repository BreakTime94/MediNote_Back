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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;
    private final NewsRepository newsRepository;

    /**
     * ✅ 1️⃣ 최신 뉴스 5~6개 (메인화면 요약용)
     * 예: GET /news/latest?limit=6
     */
    @GetMapping("/latest")
    public List<NewsPublicListItemResponseDTO> getLatest(
            @RequestParam(defaultValue = "6") int limit
    ) {
        return newsService.getLatestNews(limit);
    }

    /**
     * ✅ 2️⃣ 전체 뉴스 목록 (더보기 페이지)
     * 예: GET /news/list?page=1&size=10&sort=pubDate,desc&type=NEWS
     */
    @GetMapping("/list")
    public Page<NewsPublicListItemResponseDTO> getList(
            @ModelAttribute PageCriteria criteria,
            @RequestParam(required = false) ContentType type
    ) {
        return newsService.getNewsPage(criteria, type);
    }

    @GetMapping("/{id}/go")
    public ResponseEntity<Void> go(@PathVariable Long id) {
        var news = newsRepository.findById(id)
                .filter(News::getIsPublished)    // 비공개는 차단
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // (선택) 클릭 로깅/집계 비동기 처리
        // clickLogService.record(id);

        // 303 See Other: 안전한 GET 리디렉션
        return ResponseEntity.status(HttpStatus.SEE_OTHER)
                .location(URI.create(news.getLink()))
                .build();
    }
}
