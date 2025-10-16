package com.medinote.medinote_back_kys.news;

import com.medinote.medinote_back_kys.common.paging.PageCriteria;
import com.medinote.medinote_back_kys.news.domain.dto.NewsPublicListItemResponseDTO;
import com.medinote.medinote_back_kys.news.domain.en.ContentType;
import com.medinote.medinote_back_kys.news.domain.entity.News;
import com.medinote.medinote_back_kys.news.mapper.NewsMapper;
import com.medinote.medinote_back_kys.news.repository.NewsRepository;
import com.medinote.medinote_back_kys.news.service.NewsService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
public class NewsServiceTest {

    @Autowired private NewsService newsService;
    @Autowired private NewsRepository newsRepository;
    @Autowired private NewsMapper newsMapper;
    @Autowired private EntityManager em;

    @BeforeEach
    void clean() {
        newsRepository.deleteAllInBatch();
        em.flush();
        em.clear();
    }

    // ===== 유틸 =====
    private News seed(String title, boolean published, LocalDateTime pubDate, ContentType type) {
        News entity = News.builder()
                .sourceName("헬스경향")
                .feedUrl("https://www.k-health.com/rss/" + type.name() + ".xml")
                .title(title)
                .link("http://test.local/" + UUID.randomUUID())
                .author("테스트 기자")
                .contentType(type)
                .pubDate(pubDate)
                .description("요약 내용입니다")
                .image("http://example.com/img.png")
                .build();

        if (published) entity.approve(); else entity.reject();
        return newsRepository.save(entity);
    }

    private PageCriteria criteria(int page, int size, String... sortTokens) {
        PageCriteria c = new PageCriteria();
        c.setPage(page);
        c.setSize(size);
        if (sortTokens != null && sortTokens.length > 0) {
            c.setSort(List.of(sortTokens));
        }
        return c;
    }

    // -----------------------------------------------------
    // 1️⃣ 최신 뉴스 조회 테스트 (getLatestNews)
    // -----------------------------------------------------
    @Test
    @DisplayName("getLatestNews(limit)은 최신순으로 limit개 공개 뉴스를 반환한다")
    void getLatestNews_returns_most_recent_published_only() {
        // given
        seed("비공개-1", false, LocalDateTime.now().minusHours(1), ContentType.NEWS);
        seed("뉴스-1", true, LocalDateTime.now().minusHours(5), ContentType.NEWS);
        seed("뉴스-2", true, LocalDateTime.now().minusHours(2), ContentType.NEWS);
        seed("칼럼-1", true, LocalDateTime.now().minusHours(3), ContentType.COLUMN);
        seed("건강정보-1", true, LocalDateTime.now().minusHours(4), ContentType.HEALTH_INFO);

        // when
        List<NewsPublicListItemResponseDTO> list = newsService.getLatestNews(3);

        // then
        assertEquals(3, list.size(), "limit 개수만큼 반환해야 함");
        assertTrue(list.stream().noneMatch(n -> n.title().contains("비공개")));
        assertTrue(list.get(0).pubDate().isAfter(list.get(1).pubDate())
                || list.get(0).pubDate().isEqual(list.get(1).pubDate()));
    }

    // -----------------------------------------------------
    // 2️⃣ 페이징 뉴스 조회 테스트 (getNewsPage)
    // -----------------------------------------------------
    @Test
    @DisplayName("getNewsPage(criteria, type)은 contentType 필터와 정렬이 적용된다")
    void getNewsPage_with_type_filter_and_sorting() {
        // given
        seed("뉴스-1", true, LocalDateTime.now().minusHours(1), ContentType.NEWS);
        seed("뉴스-2", true, LocalDateTime.now().minusHours(3), ContentType.NEWS);
        seed("칼럼-1", true, LocalDateTime.now().minusHours(2), ContentType.COLUMN);
        seed("칼럼-2", true, LocalDateTime.now().minusHours(5), ContentType.COLUMN);
        seed("건강정보", true, LocalDateTime.now().minusHours(4), ContentType.HEALTH_INFO);

        PageCriteria c = criteria(1, 10, "pubDate,desc");

        // when
        Page<NewsPublicListItemResponseDTO> page = newsService.getNewsPage(c, ContentType.COLUMN);

        // then
        assertEquals(2, page.getTotalElements());
        assertTrue(page.stream().allMatch(n -> n.contentType() == ContentType.COLUMN));
        assertTrue(page.getContent().get(0).pubDate().isAfter(page.getContent().get(1).pubDate()));
    }

    // -----------------------------------------------------
    // 3️⃣ 전체 공개 뉴스 페이징 테스트 (type=null)
    // -----------------------------------------------------
    @Test
    @DisplayName("getNewsPage(criteria, null)은 모든 공개 뉴스를 반환한다")
    void getNewsPage_all_published() {
        // given
        seed("뉴스-1", true, LocalDateTime.now().minusHours(1), ContentType.NEWS);
        seed("칼럼-1", true, LocalDateTime.now().minusHours(2), ContentType.COLUMN);
        seed("건강정보", true, LocalDateTime.now().minusHours(3), ContentType.HEALTH_INFO);
        seed("비공개", false, LocalDateTime.now().minusHours(4), ContentType.NEWS);

        PageCriteria c = criteria(1, 10, "pubDate,desc");

        // when
        Page<NewsPublicListItemResponseDTO> page = newsService.getNewsPage(c, null);

        // then
        assertEquals(3, page.getTotalElements());
        assertTrue(page.stream().noneMatch(n -> n.title().contains("비공개")));
    }

    // -----------------------------------------------------
    // 4️⃣ 래퍼 목록: listNews / listColumns / listHealthInfo
    // -----------------------------------------------------
    @Test
    @DisplayName("listNews는 NEWS 타입만 반환한다")
    void listNews_only_news() {
        // given
        seed("뉴스-1", true, LocalDateTime.now().minusHours(1), ContentType.NEWS);
        seed("칼럼-1", true, LocalDateTime.now().minusHours(2), ContentType.COLUMN);

        PageCriteria c = criteria(1, 10, "pubDate,desc");

        // when
        Page<NewsPublicListItemResponseDTO> page = newsService.listNews(c);

        // then
        assertTrue(page.getTotalElements() >= 1);
        assertTrue(page.stream().allMatch(n -> n.contentType() == ContentType.NEWS));
    }

    @Test
    @DisplayName("listColumns는 COLUMN 타입만 반환한다")
    void listColumns_only_column() {
        // given
        seed("칼럼-1", true, LocalDateTime.now().minusHours(1), ContentType.COLUMN);
        seed("뉴스-1", true, LocalDateTime.now().minusHours(2), ContentType.NEWS);

        PageCriteria c = criteria(1, 10, "pubDate,desc");

        // when
        Page<NewsPublicListItemResponseDTO> page = newsService.listColumns(c);

        // then
        assertTrue(page.getTotalElements() >= 1);
        assertTrue(page.stream().allMatch(n -> n.contentType() == ContentType.COLUMN));
    }

    @Test
    @DisplayName("listHealthInfo는 HEALTH_INFO 타입만 반환한다")
    void listHealthInfo_only_health_info() {
        // given
        seed("건강정보-1", true, LocalDateTime.now().minusHours(1), ContentType.HEALTH_INFO);
        seed("뉴스-1", true, LocalDateTime.now().minusHours(2), ContentType.NEWS);

        PageCriteria c = criteria(1, 10, "pubDate,desc");

        // when
        Page<NewsPublicListItemResponseDTO> page = newsService.listHealthInfo(c);

        // then
        assertTrue(page.getTotalElements() >= 1);
        assertTrue(page.stream().allMatch(n -> n.contentType() == ContentType.HEALTH_INFO));
    }

    // -----------------------------------------------------
    // 5️⃣ 제목 검색(searchPublicByTitle) — 빈 키워드 방지 & 대소문자 무시
    // -----------------------------------------------------
    @Test
    @DisplayName("searchPublicByTitle은 빈 키워드일 경우 IllegalArgumentException을 던진다")
    void searchPublicByTitle_rejects_blank_keyword() {
        // given
        PageCriteria c = criteria(1, 10, "pubDate,desc");

        // then
        assertThrows(IllegalArgumentException.class,
                () -> newsService.searchPublicByTitle(c, null, "  "));
    }

    @Test
    @DisplayName("searchPublicByTitle은 공개글만 대상으로 제목에 키워드가 포함된 것만 반환한다(타입 무관)")
    void searchPublicByTitle_filters_published_and_title_contains_all_types() {
        // given
        seed("면역 체계 강화법", true, LocalDateTime.now().minusHours(1), ContentType.NEWS);
        seed("수면과 건강",     true, LocalDateTime.now().minusHours(2), ContentType.COLUMN);
        seed("면역 식단 가이드", false, LocalDateTime.now().minusHours(3), ContentType.HEALTH_INFO); // 비공개

        PageCriteria c = criteria(1, 10, "pubDate,desc");

        // when
        Page<NewsPublicListItemResponseDTO> page =
                newsService.searchPublicByTitle(c, null, "면역");

        // then
        assertEquals(1, page.getTotalElements());
        assertTrue(page.getContent().get(0).title().contains("면역"));
    }

    @Test
    @DisplayName("searchPublicByTitle은 타입 필터와 제목 키워드를 동시에 적용한다")
    void searchPublicByTitle_with_type_filter() {
        // given
        seed("비만 관리 가이드", true, LocalDateTime.now().minusHours(1), ContentType.COLUMN);
        seed("비만과 대사증후군", true, LocalDateTime.now().minusHours(2), ContentType.NEWS);
        seed("비만 예방 식단",   true, LocalDateTime.now().minusHours(3), ContentType.HEALTH_INFO);

        PageCriteria c = criteria(1, 10, "pubDate,desc");

        // when — COLUMN만 + '비만'
        Page<NewsPublicListItemResponseDTO> page =
                newsService.searchPublicByTitle(c, ContentType.COLUMN, "비만");

        // then
        assertEquals(1, page.getTotalElements());
        assertEquals(ContentType.COLUMN, page.getContent().get(0).contentType());
        assertTrue(page.getContent().get(0).title().contains("비만"));
    }

    @Test
    @DisplayName("searchPublicByTitle는 대소문자 무시를 보장한다(환경이 *_ci가 아닐 경우 Repository를 lower(...)로 교체 필요)")
    void searchPublicByTitle_case_insensitive() {
        // given (영문 타이틀로 케이스 확인)
        seed("IMMUNE System Tips", true, LocalDateTime.now().minusHours(1), ContentType.NEWS);
        seed("Sleep and Health",    true, LocalDateTime.now().minusHours(2), ContentType.COLUMN);

        PageCriteria c = criteria(1, 10, "pubDate,desc");

        // when: 소문자 키워드로 검색
        Page<NewsPublicListItemResponseDTO> page =
                newsService.searchPublicByTitle(c, null, "immune");

        // then
        // 대부분의 MariaDB *_ci 콜레이션에서는 통과합니다.
        // 만약 환경이 CS라면 Repository의 JPQL을 lower(...)로 바꾸셔야 합니다.
        assertEquals(1, page.getTotalElements());
        assertTrue(page.getContent().get(0).title().toLowerCase().contains("immune"));
    }
}
