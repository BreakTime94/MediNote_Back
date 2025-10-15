package com.medinote.medinote_back_kys.news.service;

import com.medinote.medinote_back_kys.common.paging.NewsSortWhitelist;
import com.medinote.medinote_back_kys.common.paging.PageCriteria;
import com.medinote.medinote_back_kys.news.domain.dto.*;
import com.medinote.medinote_back_kys.news.domain.en.ContentType;
import com.medinote.medinote_back_kys.news.domain.entity.News;
import com.medinote.medinote_back_kys.news.mapper.NewsMapper;
import com.medinote.medinote_back_kys.news.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsService {

    private final NewsRepository newsRepository;
    private final NewsMapper newsMapper;

    // --------------------------------------------------------------------
    // ✅ 1️⃣ 메인화면용 최신 뉴스 (5~6개)
    // --------------------------------------------------------------------
    public List<NewsPublicListItemResponseDTO> getLatestNews(int limit) {
        Pageable pageable = PageRequest.of(0, limit,
                Sort.by(Sort.Order.desc("pubDate"), Sort.Order.desc("id")));

        Page<News> page = newsRepository.findByIsPublishedTrue(pageable);

        return page.stream()
                .map(newsMapper::toPublicListItem)
                .toList();
    }

    // --------------------------------------------------------------------
    // ✅ 2️⃣ 전체 뉴스 목록 (사용자 “더보기 페이지”)
    // --------------------------------------------------------------------
    public Page<NewsPublicListItemResponseDTO> getNewsPage(PageCriteria criteria, ContentType contentType) {
        Pageable pageable = criteria.toPageable(NewsSortWhitelist.PUBLIC);

        Page<News> page = newsRepository.findPublicList(contentType, pageable);
        return page.map(newsMapper::toPublicListItem);
    }

    // --------------------------------------------------------------------
    // ✅ 3️⃣ 관리자 목록 조회 (검색/필터/정렬)
    // --------------------------------------------------------------------
    public Page<AdminNewsListItemResponseDTO> getAdminList(PageCriteria criteria,
                                                           ContentType type,
                                                           Boolean published,
                                                           String keyword) {
        Pageable pageable = criteria.toPageable(NewsSortWhitelist.ADMIN);

        Page<News> page = newsRepository.searchAdmin(type, published, keyword, pageable);
        return page.map(newsMapper::toAdminListItem);
    }

    // --------------------------------------------------------------------
    // ✅ 4️⃣ 관리자 상세 조회
    // --------------------------------------------------------------------
    public AdminNewsDetailResponseDTO getAdminDetail(Long id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("뉴스를 찾을 수 없습니다. id=" + id));

        return newsMapper.toAdminDetail(news);
    }

    // --------------------------------------------------------------------
    // ✅ 5️⃣ 관리자 공개상태 변경 (단일/다중 모두 지원)
    // --------------------------------------------------------------------
    @Transactional
    public List<AdminNewsPublishResponseDTO> updatePublishStatus(List<Long> ids, Boolean published) {
        int updatedCount = newsRepository.updatePublishStatus(ids, published);
        log.info("✅ {}건의 뉴스 공개 상태가 {}로 변경되었습니다.", updatedCount, published);

        List<News> updatedList = newsRepository.findAllById(ids);
        return updatedList.stream()
                .map(newsMapper::toAdminPublishResponse)
                .toList();
    }
}
