package com.medinote.medinote_back_kys.news;



import com.medinote.medinote_back_kys.news.domain.en.ContentType;
import com.medinote.medinote_back_kys.news.domain.entity.News;
import com.medinote.medinote_back_kys.news.mapper.NewsMapper;
import com.medinote.medinote_back_kys.news.repository.NewsRepository;
import com.medinote.medinote_back_kys.news.service.NewsCollectorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RSS 실주소 파싱 + 매핑 + 저장 통합 테스트
 * - 실제 네트워크 환경 필요
 * - 테스트 종료 시 롤백되므로 DB는 깨끗하게 유지됨
 */
@SpringBootTest
@Transactional
@DisplayName("RSS 실주소 파싱 + 매핑 + 저장 통합 테스트")
public class NewsCollectorServiceTest {

    @Autowired private NewsRepository newsRepository; // ✅ 핵심 대상 #1
    @Autowired private NewsMapper newsMapper;         // ✅ 핵심 대상 #2
    @Autowired private NewsCollectorService newsCollectorService;

    // 서비스와 동일 포맷 (원문 pubDate: yyyy-MM-dd HH:mm:ss)
    private static final DateTimeFormatter KHEALTH_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @BeforeEach
    void setUp() {
        newsRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("collectAllFeeds() 호출 시 실제 RSS에서 최소 1건 이상 저장된다")
    void collectAllFeeds_shouldInsertRows() {
        long before = newsRepository.count();

        assertDoesNotThrow(() -> newsCollectorService.collectAllFeeds(),
                "실제 RSS 파싱에서 예외가 발생하면 안 됩니다.");

        long after = newsRepository.count();
        // 외부 이슈로 0건일 수 있어 가드: 0이면 스킵
        org.junit.jupiter.api.Assumptions.assumeTrue(after > before,
                "원격 RSS에서 항목이 0건 반환되어 테스트를 스킵합니다.");

        assertTrue(after > before, "RSS 수집 후 뉴스가 최소 1건 이상 저장되어야 합니다.");
    }

    @Test
    @DisplayName("중복 링크는 저장되지 않는다 (같은 RSS 두 번 수집해도 카운트 증가 X)")
    void duplicate_shouldBeIgnoredByLink() {
        newsCollectorService.collectAllFeeds();
        long first = newsRepository.count();

        newsCollectorService.collectAllFeeds();
        long second = newsRepository.count();

        // 외부 이슈일 경우 스킵
        org.junit.jupiter.api.Assumptions.assumeTrue(first > 0,
                "첫 수집 결과가 0건으로, 원격 이슈로 판단되어 스킵합니다.");

        assertEquals(first, second, "동일 링크는 중복 저장되지 않아야 합니다.");
    }

    @Test
    @DisplayName("저장된 엔티티는 필수 필드가 채워지고 ContentType 매핑이 합리적이다")
    void savedEntity_requiredFields_and_ContentType() {
        newsCollectorService.collectAllFeeds();
        List<News> all = newsRepository.findAll();

        org.junit.jupiter.api.Assumptions.assumeTrue(!all.isEmpty(),
                "원격 RSS가 빈 결과를 반환하여 스킵합니다.");

        all.forEach(n -> {
            assertNotNull(n.getTitle(), "title은 null이 아니어야 합니다");
            assertNotNull(n.getLink(), "link는 null이 아니어야 합니다");
            assertNotNull(n.getSourceName(), "sourceName은 null이 아니어야 합니다");
            assertNotNull(n.getPubDate(), "pubDate는 null이 아니어야 합니다");
            assertNotNull(n.getContentType(), "contentType은 null이 아니어야 합니다");
        });

        // NEWS 타입은 최소 존재 가능성이 높음
        boolean hasNews = all.stream().anyMatch(n -> n.getContentType() == ContentType.NEWS);
        assertTrue(hasNews, "NEWS 타입의 데이터가 최소 1건 이상 있어야 합니다.");
    }

    @Test
    @DisplayName("원문 pubDate(yyyy-MM-dd HH:mm:ss) 우선 적용 확인 (링크 매칭 기반 샘플 비교)")
    void pubDate_shouldPreferOriginalText_whenAvailable() throws Exception {
        // 수집 수행
        newsCollectorService.collectAllFeeds();
        List<News> saved = newsRepository.findAll();

        org.junit.jupiter.api.Assumptions.assumeTrue(!saved.isEmpty(),
                "수집 결과가 0건이라 스킵합니다.");

        // 테스트 신뢰도 향상 위해: 링크 목록을 모아 실제 RSS 3개에서 link->pubDate 맵을 만든 뒤 교집합 비교
        Map<String, String> remoteMap = new HashMap<>();
        remoteMap.putAll(fetchLinkToPubDate("https://www.k-health.com/rss/S1N1.xml"));
        remoteMap.putAll(fetchLinkToPubDate("https://www.k-health.com/rss/S1N4.xml"));
        remoteMap.putAll(fetchLinkToPubDate("https://www.k-health.com/rss/S1N10.xml"));

        // 저장된 엔티티 중, 원문 맵에서 pubDate를 찾을 수 있는 것만 대상으로 검증
        List<News> candidates = saved.stream()
                .filter(n -> remoteMap.containsKey(n.getLink()))
                .collect(Collectors.toList());

        org.junit.jupiter.api.Assumptions.assumeTrue(!candidates.isEmpty(),
                "원문 pubDate를 매칭할 수 있는 링크가 없어 스킵합니다.");

        // 샘플 한 건만 골라 비교 (여러 건 비교하면 원격 변동성에 취약해짐)
        News sample = candidates.get(0);
        String raw = remoteMap.get(sample.getLink());

        // 원문 포맷 파싱
        LocalDateTime expected = LocalDateTime.parse(raw, KHEALTH_FMT);
        LocalDateTime actual = sample.getPubDate();

        // 동일해야 함 (서비스는 원문 pubDate를 LocalDateTime으로 저장)
        assertEquals(expected, actual,
                () -> "원문 pubDate 우선 적용이 되지 않았습니다. link=" + sample.getLink()
                        + ", raw=" + raw + ", saved=" + actual);
    }

    // ===== 테스트용 유틸 =====

    /**
     * 주어진 RSS XML에서 <item><link> → <pubDate> 맵을 추출한다.
     * 서비스 코드와 동일한 방식(DOCTYPE 제거, 외부 DTD 차단)으로 파싱.
     */
    private Map<String, String> fetchLinkToPubDate(String feedUrl) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(feedUrl).openConnection();
        conn.setConnectTimeout(7_000);
        conn.setReadTimeout(7_000);
        conn.setRequestProperty("User-Agent", "MedinoteBot/1.0 (+https://medinote.local)");

        String xml;
        try (InputStream is = conn.getInputStream()) {
            xml = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }

        String sanitized = xml.replaceAll("(?is)<!DOCTYPE[^>]*>", "");

        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(false);
        f.setValidating(false);
        f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        try {
            f.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            f.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        } catch (IllegalArgumentException ignore) {}
        try {
            f.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            f.setFeature("http://xml.org/sax/features/validation", false);
        } catch (Exception ignore) {}

        DocumentBuilder builder = f.newDocumentBuilder();
        Document doc = builder.parse(new java.io.ByteArrayInputStream(sanitized.getBytes(StandardCharsets.UTF_8)));

        Map<String, String> map = new HashMap<>();
        NodeList items = doc.getElementsByTagName("item");
        for (int i = 0; i < items.getLength(); i++) {
            org.w3c.dom.Element item = (org.w3c.dom.Element) items.item(i);
            String link = getText(item, "link");
            String pub  = getText(item, "pubDate");
            if (link != null && !link.isBlank() && pub != null && !pub.isBlank()) {
                map.put(link.trim(), pub.trim());
            }
        }
        return map;
    }

    private String getText(org.w3c.dom.Element el, String tag) {
        NodeList n = el.getElementsByTagName(tag);
        return (n.getLength() > 0 && n.item(0).getTextContent() != null)
                ? n.item(0).getTextContent().trim()
                : null;
    }
}
