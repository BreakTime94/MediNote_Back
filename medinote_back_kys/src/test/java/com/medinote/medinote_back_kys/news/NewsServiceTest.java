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

    @Autowired
    private NewsService newsService;
    @Autowired
    private NewsRepository newsRepository;
    @Autowired
    private NewsMapper newsMapper;
    @Autowired
    private EntityManager em;

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

        // 수동 발행 여부 설정
        if (published) entity.approve();
        else entity.reject();

        return newsRepository.save(entity);
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
        assertTrue(list.stream().allMatch(n -> n.contentType() != null));
        assertTrue(list.stream().noneMatch(n -> n.title().contains("비공개")),
                "비공개 뉴스는 포함되지 않아야 함");

        // 정렬 검증 (pubDate DESC)
        assertTrue(list.get(0).pubDate().isAfter(list.get(1).pubDate())
                        || list.get(0).pubDate().isEqual(list.get(1).pubDate()),
                "최신순으로 정렬되어야 함");

        System.out.println("===== 최신 뉴스(Top 3) =====");
        list.forEach(n ->
                System.out.printf("%s | %s | %s%n",
                        n.contentType(), n.title(), n.pubDate())
        );
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

        // page=1(size=10), sort=pubDate,desc
        PageCriteria criteria = new PageCriteria();
        criteria.setPage(1);
        criteria.setSize(10);
        criteria.setSort(List.of("pubDate,desc"));

        // when
        Page<NewsPublicListItemResponseDTO> page = newsService.getNewsPage(criteria, ContentType.COLUMN);

        // then
        assertEquals(2, page.getTotalElements(), "COLUMN 타입 뉴스만 조회되어야 함");
        assertTrue(page.stream().allMatch(n -> n.contentType() == ContentType.COLUMN));
        assertTrue(page.getContent().get(0).pubDate().isAfter(page.getContent().get(1).pubDate()),
                "pubDate DESC 정렬되어야 함");

        System.out.println("===== COLUMN 타입 최신순 페이지 =====");
        page.forEach(n ->
                System.out.printf("[#%d] %s | %s | %s%n",
                        n.id(), n.contentType(), n.title(), n.pubDate())
        );
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

        PageCriteria criteria = new PageCriteria();
        criteria.setPage(1);
        criteria.setSize(10);
        criteria.setSort(List.of("pubDate,desc"));

        // when
        Page<NewsPublicListItemResponseDTO> page = newsService.getNewsPage(criteria, null);

        // then
        assertEquals(3, page.getTotalElements(), "공개 뉴스만 반환해야 함");
        assertTrue(page.stream().noneMatch(n -> n.title().contains("비공개")));

        // 기본 정렬 확인
        List<NewsPublicListItemResponseDTO> list = page.getContent();
        assertTrue(list.get(0).pubDate().isAfter(list.get(1).pubDate())
                        || list.get(0).pubDate().isEqual(list.get(1).pubDate()),
                "pubDate DESC 정렬되어야 함");

        System.out.println("===== 전체 공개 뉴스 =====");
        list.forEach(n ->
                System.out.printf("[#%d] %s | %s | %s%n",
                        n.id(), n.contentType(), n.title(), n.pubDate())
        );
    }
}
