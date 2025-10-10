package com.medinote.medinote_back_kys.news;

import com.medinote.medinote_back_kys.news.domain.dto.*;
import com.medinote.medinote_back_kys.news.domain.en.ContentType;
import com.medinote.medinote_back_kys.news.domain.entity.News;
import com.medinote.medinote_back_kys.news.mapper.NewsMapper;
import com.medinote.medinote_back_kys.news.repository.NewsRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;


import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional // 각 테스트 종료 시 롤백
class NewsRepositoryTest {

    @Autowired private NewsRepository newsRepository;
    @Autowired private NewsMapper newsMapper;
    @Autowired private EntityManager em;

    @BeforeEach
    void clean() {
        newsRepository.deleteAllInBatch();
        em.flush();
        em.clear();
    }

    // --- 유틸 ---
    private static String unique(String seed) {
        return seed + "-" + UUID.randomUUID();
    }

    private static Pageable page(int p, int s, Sort sort) {
        return PageRequest.of(p, s, sort);
    }

    private News seed(String title, String linkSeed, boolean published,
                      LocalDateTime pubDate, String sectionCode) {

        // DTO → Entity
        NewsSaveRequestDTO dto = new NewsSaveRequestDTO(
                "헬스경향",
                "http://rss.k-health.com",
                title,
                "http://test.local/" + unique(linkSeed),
                "장인선 기자",
                sectionCode,
                "S2N67",
                sectionCode,
                "본문 요약",
                "http://example.com/img.png",
                pubDate
        );

        News entity = newsMapper.toEntity(dto);

        // contentType 자동 결정 (DB가 아닌 테스트 로직 내에서)
        ContentType type = switch (sectionCode) {
            case "S1N10" -> ContentType.HEALTH_INFO;
            case "S1N4"  -> ContentType.COLUMN;
            default      -> ContentType.NEWS;
        };

        entity = entity.toBuilder()
                .contentType(type)
                .build();

        // 공개 여부 조정
        if (published) entity.approve(); else entity.reject();

        return newsRepository.save(entity);
    }

    // ----------------------------------------------------
    // 1. 기본 CRUD 동작 검증
    // ----------------------------------------------------
    @Test
    void save_findByLink_existsByLink() {
        News saved = seed("제목1", "lk-1001", false, LocalDateTime.now().minusHours(3), "S1N1");
        String link = saved.getLink();

        assertTrue(newsRepository.findByLink(link).isPresent());
        assertTrue(newsRepository.existsByLink(link));
        assertFalse(newsRepository.existsByLink("http://test.local/none"));
    }

    @Test
    void findByIsPublishedTrue_only() {
        seed("공개1", "p1", true,  LocalDateTime.now().minusHours(1), "S1N1");
        seed("비공개1","p2", false, LocalDateTime.now().minusHours(2), "S1N1");
        seed("공개2", "p3", true,  LocalDateTime.now().minusHours(3), "S1N10");

        Page<News> page = newsRepository.findByIsPublishedTrue(
                page(0, 10, Sort.by(Sort.Order.desc("pubDate"), Sort.Order.desc("id")))
        );
        assertEquals(2, page.getTotalElements());
        assertTrue(page.stream().allMatch(News::getIsPublished));
    }

    // ----------------------------------------------------
    // 2. 검색 / 필터 쿼리 검증
    // ----------------------------------------------------
    @Test
    void searchPublic_keyword_in_title_description_author() {
        seed("간질환 공개강좌", "k1", true,  LocalDateTime.now().minusHours(1), "S1N1");
        seed("제목무관",     "k2", true,  LocalDateTime.now().minusHours(2), "S1N1");
        seed("비공개-제외",  "k3", false, LocalDateTime.now().minusHours(3), "S1N1");

        Page<News> page = newsRepository.searchPublic(
                "공개강좌", page(0, 10, Sort.by("id").descending())
        );

        assertEquals(1, page.getTotalElements());
        assertEquals("간질환 공개강좌", page.getContent().get(0).getTitle());
    }

    @Test
    void bulk_update_publishStatus() {
        News a = seed("A", "ba", false, LocalDateTime.now().minusHours(1), "S1N1");
        News b = seed("B", "bb", false, LocalDateTime.now().minusHours(2), "S1N4");

        int updated = newsRepository.updatePublishStatus(List.of(a.getId(), b.getId()), true);
        assertEquals(2, updated);

        Page<News> published = newsRepository.findByIsPublishedTrue(page(0, 10, Sort.by("id").descending()));
        Set<Long> ids = new HashSet<>();
        published.forEach(n -> ids.add(n.getId()));
        assertTrue(ids.contains(a.getId()));
        assertTrue(ids.contains(b.getId()));
    }

    @Test
    void admin_search_with_contentType_filter() {
        seed("뉴스-공개",   "a1", true,  LocalDateTime.now().minusMinutes(5), "S1N1");
        seed("뉴스-비공개", "a2", false, LocalDateTime.now().minusMinutes(10), "S1N1");
        seed("칼럼-공개",   "a3", true,  LocalDateTime.now().minusMinutes(20), "S1N4");
        seed("건강정보",   "a4", true,  LocalDateTime.now().minusMinutes(25), "S1N10");

        Pageable pageable = page(0, 10, Sort.by("id").descending());

        Page<News> newsOnly = newsRepository.findByContentType(ContentType.NEWS, pageable);
        Page<News> columnOnly = newsRepository.findByContentType(ContentType.COLUMN, pageable);
        Page<News> healthOnly = newsRepository.findByContentType(ContentType.HEALTH_INFO, pageable);

        assertEquals(2, newsOnly.getTotalElements());
        assertEquals(1, columnOnly.getTotalElements());
        assertEquals(1, healthOnly.getTotalElements());
    }

    // ----------------------------------------------------
    // 3. Mapper 검증 (Setter 없이)
    // ----------------------------------------------------
    @Test
    void mapper_end_to_end_without_setters() {
        NewsSaveRequestDTO req = new NewsSaveRequestDTO(
                "헬스경향",
                "http://www.k-health.com/rss",
                "매퍼 저장 테스트",
                "http://www.k-health.com/news/articleView.html?idxno=" + UUID.randomUUID(),
                "테스트 기자",
                "S1N1",
                "S2N67",
                "S1N1",
                "요약입니다",
                "http://example.com/img.png",
                LocalDateTime.now().minusHours(2)
        );

        News entity = newsMapper.toEntity(req);
        assertNotNull(entity);
        assertFalse(entity.getIsPublished());

        // contentType 수동 설정 (서비스 대신)
        entity = entity.toBuilder().contentType(ContentType.NEWS).build();
        entity = newsRepository.save(entity);

        // Public DTO
        NewsPublicListItemResponseDTO pub = newsMapper.toPublicListItem(entity);
        assertEquals(entity.getId(), pub.id());
        assertEquals(entity.getTitle(), pub.title());
        assertEquals(entity.getContentType(), pub.contentType());

        // Admin List DTO
        AdminNewsListItemResponseDTO adminItem = newsMapper.toAdminListItem(entity);
        assertEquals(entity.getId(), adminItem.id());
        assertEquals(entity.getContentType(), adminItem.contentType());

        // Admin Detail DTO
        AdminNewsDetailResponseDTO detail = newsMapper.toAdminDetail(entity);
        assertEquals(entity.getLink(), detail.link());
        assertEquals(entity.getDescription(), detail.description());

        // 발행 응답 DTO
        entity.approve();
        AdminNewsPublishResponseDTO pubRes = newsMapper.toAdminPublishResponse(entity);
        assertTrue(pubRes.isPublished());

        // 수집/등록 완료 DTO
        LocalDateTime now = LocalDateTime.now();
        NewsIngestResponseDTO ingest = newsMapper.toIngestResponse(entity, now);
        assertEquals(now, ingest.ingestedAt());
        assertEquals(entity.getContentType(), ingest.contentType());
    }
}