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
import org.springframework.util.StringUtils;

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
    // ✅ 1) 메인화면용 최신 뉴스 (5~6개)
    // --------------------------------------------------------------------
    public List<NewsPublicListItemResponseDTO> getLatestNews(int limit) {
        int safe = Math.min(Math.max(limit, 1), 20); // 1~20 가드
        Pageable pageable = PageRequest.of(0, safe,
                Sort.by(Sort.Order.desc("pubDate"), Sort.Order.desc("id")));

        Page<News> page = newsRepository.findByIsPublishedTrue(pageable);
        return page.stream()
                .map(newsMapper::toPublicListItem)
                .toList();
    }

    // --------------------------------------------------------------------
    // ✅ 2) 공개 목록 (전체/타입별) - 사용자 더보기 페이지 공용
    // --------------------------------------------------------------------
    public Page<NewsPublicListItemResponseDTO> getNewsPage(PageCriteria criteria, ContentType contentType) {
        Pageable pageable = criteria.toPageable(NewsSortWhitelist.PUBLIC);
        Page<News> page = newsRepository.findPublicList(contentType, pageable);
        return page.map(newsMapper::toPublicListItem);
    }

    // ✅ 2-a) 뉴스 전용
    public Page<NewsPublicListItemResponseDTO> listNews(PageCriteria criteria) {
        return getNewsPage(criteria, ContentType.NEWS);
    }

    // ✅ 2-b) 칼럼 전용
    public Page<NewsPublicListItemResponseDTO> listColumns(PageCriteria criteria) {
        return getNewsPage(criteria, ContentType.COLUMN);
    }

    // ✅ 2-c) 건강정보 전용
    public Page<NewsPublicListItemResponseDTO> listHealthInfo(PageCriteria criteria) {
        return getNewsPage(criteria, ContentType.HEALTH_INFO);
    }

    // --------------------------------------------------------------------
    // ✅ 3) 공개 제목검색 (타입 옵션)
    //  - 빈 키워드 방지: 공백/빈 문자열이면 IllegalArgumentException
    //  - 대소문자 무시: MariaDB *_ci 콜레이션이면 LIKE 자체가 case-insensitive
    //                  만약 환경이 CS라면 Repository의 JPQL을 lower(...)로 변경 권장
    // --------------------------------------------------------------------
    public Page<NewsPublicListItemResponseDTO> searchPublicByTitle(PageCriteria criteria,
                                                                   ContentType type,
                                                                   String keyword) {
        if (!StringUtils.hasText(keyword)) {
            throw new IllegalArgumentException("검색 키워드는 비어 있을 수 없습니다.");
        }
        String kw = keyword.trim();
        Pageable pageable = criteria.toPageable(NewsSortWhitelist.PUBLIC);

        // (참고) MariaDB 기본 콜레이션이 *_ci면 대소문자 무시됨.
        // 만약 대소문자 구분 환경이면 repository의 JPQL을
        //   lower(n.title) like lower(concat('%', :keyword, '%'))
        // 로 바꾸는 것을 권장.
        return newsRepository.searchPublicByTitle(type, kw, pageable)
                .map(newsMapper::toPublicListItem);
    }

    // --------------------------------------------------------------------
    // ✅ 4) 관리자 목록 조회 (검색/필터/정렬)
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
    // ✅ 5) 관리자 상세 조회
    // --------------------------------------------------------------------
    public AdminNewsDetailResponseDTO getAdminDetail(Long id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("뉴스를 찾을 수 없습니다. id=" + id));
        return newsMapper.toAdminDetail(news);
    }

    // --------------------------------------------------------------------
    // ✅ 6) 관리자 공개상태 변경 (단일/다중 모두 지원)
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
