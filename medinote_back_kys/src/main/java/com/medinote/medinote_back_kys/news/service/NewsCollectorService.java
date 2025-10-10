package com.medinote.medinote_back_kys.news.service;

import com.medinote.medinote_back_kys.news.domain.dto.NewsSaveRequestDTO;
import com.medinote.medinote_back_kys.news.domain.en.ContentType;
import com.medinote.medinote_back_kys.news.domain.entity.News;
import com.medinote.medinote_back_kys.news.mapper.NewsMapper;
import com.medinote.medinote_back_kys.news.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdom2.Element;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.rometools.rome.feed.synd.*;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * ROME + 원문 pubDate(K-Health: yyyy-MM-dd HH:mm:ss) 우선 적용
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NewsCollectorService {

    private final NewsRepository newsRepository;
    private final NewsMapper newsMapper;

    private static final Map<String, String> RSS_FEEDS = Map.of(
            "S1N1",  "https://www.k-health.com/rss/S1N1.xml",   // 뉴스
            "S1N4",  "https://www.k-health.com/rss/S1N4.xml",   // 칼럼
            "S1N10", "https://www.k-health.com/rss/S1N10.xml"   // 건강정보
    );

    // k-health 원문 pubDate 형식
    private static final DateTimeFormatter KHEALTH_PUBDATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Transactional
    public void collectAllFeeds() {
        RSS_FEEDS.forEach((sectionCode, feedUrl) -> {
            try {
                collectFeed(sectionCode, feedUrl);
            } catch (Exception e) {
                log.error("❌ RSS 수집 실패 [{}]: {}", sectionCode, e.toString());
            }
        });
    }

    private void collectFeed(String sectionCode, String feedUrl) throws Exception {
        log.info("📡 [{}] RSS 수집 시작: {}", sectionCode, feedUrl);

        // 1) HTTP 요청 (한 번만 다운로드)
        HttpURLConnection conn = (HttpURLConnection) new URL(feedUrl).openConnection();
        conn.setConnectTimeout(7_000);
        conn.setReadTimeout(7_000);
        conn.setRequestProperty("User-Agent", "MedinoteBot/1.0 (+https://medinote.local)");

        String xml;
        try (InputStream is = conn.getInputStream()) {
            xml = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }

        // 2) 원문 XML에서 <link> → <pubDate> 매핑 추출 (DOM, DTD/검증 비활성 + DOCTYPE 제거)
        Map<String, String> linkToRawPubDate = extractLinkToPubDate(xml);

        // 3) 같은 XML을 ROME으로 파싱
        SyndFeedInput input = new SyndFeedInput();
        try (XmlReader reader = new XmlReader(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)))) {
            SyndFeed feed = input.build(reader);

            for (SyndEntry entry : feed.getEntries()) {
                String title = str(entry.getTitle());
                String link  = pickBestLink(entry);
                String author = str(entry.getAuthor());
                String desc = extractDescription(entry);

                // 커스텀 태그 (section_code 등)
                String itemSectionCode     = getForeignValue(entry, "section_code");
                String itemSubSectionCode  = getForeignValue(entry, "sub_section_code");
                String itemSerialCode      = getForeignValue(entry, "serial_code");

                // ✅ 원문 pubDate 우선 적용
                LocalDateTime pubDate = resolvePublishedAtWithOverride(entry, link, linkToRawPubDate);

                if (link == null || link.isBlank()) continue;
                if (newsRepository.existsByLink(link)) continue;

                NewsSaveRequestDTO dto = new NewsSaveRequestDTO(
                        feed.getTitle() != null ? feed.getTitle().trim() : "헬스경향",
                        feedUrl,
                        title,
                        link,
                        author,
                        (itemSectionCode != null ? itemSectionCode : sectionCode),
                        null,
                        itemSubSectionCode,
                        desc,
                        null,
                        pubDate
                );

                String finalSection = dto.sectionCode();
                News entity = newsMapper.toEntity(dto).toBuilder()
                        .contentType(resolveContentType(finalSection != null ? finalSection : sectionCode))
                        .serialCode(itemSerialCode)
                        .build();

                newsRepository.save(entity);
            }
        }

        log.info("✅ [{}] RSS 수집 완료", sectionCode);
    }

    /** XML 원문에서 <item><link>, <pubDate> 매핑 추출 */
    private Map<String, String> extractLinkToPubDate(String xml) throws Exception {
        // DOCTYPE 전부 제거 (비정상 DOCTYPE 방어)
        String sanitized = xml.replaceAll("(?is)<!DOCTYPE[^>]*>", "");

        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(false);
        f.setValidating(false);
        f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        // 외부 DTD/스키마 접근 차단
        try {
            f.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            f.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        } catch (IllegalArgumentException ignore) {}

        // 일부 구현체용 플래그
        try {
            f.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            f.setFeature("http://xml.org/sax/features/validation", false);
        } catch (Exception ignore) {}

        DocumentBuilder builder = f.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(sanitized.getBytes(StandardCharsets.UTF_8)));

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

    private String str(String s) { return (s == null) ? null : s.trim(); }

    /** 링크 우선순위: link → uri → enclosure.url */
    private String pickBestLink(SyndEntry entry) {
        if (entry.getLink() != null && !entry.getLink().isBlank()) return entry.getLink();
        if (entry.getUri()  != null && !entry.getUri().isBlank())  return entry.getUri();
        List<SyndEnclosure> enc = entry.getEnclosures();
        if (enc != null) {
            return enc.stream()
                    .map(SyndEnclosure::getUrl)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    private String extractDescription(SyndEntry entry) {
        if (entry.getDescription() != null && entry.getDescription().getValue() != null) {
            return entry.getDescription().getValue().trim();
        }
        List<SyndContent> contents = entry.getContents();
        if (contents != null && !contents.isEmpty()) {
            String v = contents.get(0).getValue();
            if (v != null) return v.trim();
        }
        return null;
    }

    /** 🔸원문 pubDate 우선, 없으면 ROME 날짜 사용 */
    private LocalDateTime resolvePublishedAtWithOverride(SyndEntry entry, String link, Map<String, String> linkToRaw) {
        // 1) 원문 pubDate(yyyy-MM-dd HH:mm:ss) → Asia/Seoul 가정
        if (link != null && linkToRaw != null) {
            String raw = linkToRaw.get(link);
            if (raw != null && !raw.isBlank()) {
                try {
                    // 원문은 타임존 미표기 → KST로 해석 후 LocalDateTime 반환
                    return LocalDateTime.parse(raw.trim(), KHEALTH_PUBDATE_FMT);
                } catch (Exception ignore) {}
            }
        }
        // 2) ROME의 published/updated
        Date d = entry.getPublishedDate();
        if (d == null) d = entry.getUpdatedDate();
        if (d == null) return LocalDateTime.now();
        return Instant.ofEpochMilli(d.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /** foreignMarkup에서 커스텀 태그(section_code 등) 값 추출 */
    private String getForeignValue(SyndEntry entry, String tagName) {
        List<Element> fm = entry.getForeignMarkup();
        if (fm == null) return null;
        for (Element e : fm) {
            if (tagName.equalsIgnoreCase(e.getName())) {
                String v = e.getTextNormalize();
                if (v != null && !v.isBlank()) return v;
            }
        }
        return null;
    }

    private ContentType resolveContentType(String sectionCode) {
        if (sectionCode == null) return ContentType.NEWS;
        return switch (sectionCode) {
            case "S1N10" -> ContentType.HEALTH_INFO;
            case "S1N4"  -> ContentType.COLUMN;
            default      -> ContentType.NEWS;
        };
    }
}