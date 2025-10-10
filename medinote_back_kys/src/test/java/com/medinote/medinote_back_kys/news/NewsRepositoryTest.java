package com.medinote.medinote_back_kys.news;

import com.medinote.medinote_back_kys.news.domain.dto.NewsSaveRequestDTO;
import com.medinote.medinote_back_kys.news.domain.entity.News;
import com.medinote.medinote_back_kys.news.domain.mapper.NewsMapper;
import com.medinote.medinote_back_kys.news.repository.NewsRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@SpringBootTest
@Transactional
public class NewsRepositoryTest {

    @Autowired
    private NewsRepository newsRepository;

    @Autowired
    private NewsMapper newsMapper;

    @Test
    @DisplayName("뉴스 저장 및 단일 조회 테스트")
    void testSaveAndFindById() {
        // given
        NewsSaveRequestDTO dto = new NewsSaveRequestDTO(
                "헬스경향",
                "http://www.k-health.com/rss",
                "테스트 기사 제목",
                "http://www.k-health.com/news/test123",
                "홍길동 기자",
                "S1N1",
                "S2N2",
                "S1N1",
                "테스트 기사 내용입니다.",
                "http://image.test/sample.jpg",
                LocalDateTime.now()
        );

        News entity = newsMapper.toEntity(dto);

        // when
        News saved = newsRepository.save(entity);

        // then
        Optional<News> foundOpt = newsRepository.findById(saved.getId());
        Assertions.assertTrue(foundOpt.isPresent());
        News found = foundOpt.get();

        Assertions.assertEquals(dto.title(), found.getTitle());
        Assertions.assertEquals(dto.link(), found.getLink());
        Assertions.assertFalse(found.getIsPublished());
    }

    @Test
    @DisplayName("링크 중복 조회 테스트")
    void testFindByLink() {
        // given
        String link = "http://www.k-health.com/news/test456";

        News news = News.builder()
                .sourceName("헬스경향")
                .feedUrl("http://www.k-health.com/rss")
                .title("중복 테스트")
                .link(link)
                .author("테스터 기자")
                .pubDate(LocalDateTime.now())
                .isPublished(false)
                .build();

        newsRepository.save(news);

        // when
        Optional<News> found = newsRepository.findByLink(link);

        // then
        Assertions.assertTrue(found.isPresent());
        Assertions.assertEquals(link, found.get().getLink());
    }

    @Test
    @DisplayName("승인된 뉴스만 조회 테스트")
    void testFindAllByIsPublishedTrue() {
        // given
        News approved = News.builder()
                .sourceName("헬스경향")
                .feedUrl("http://rss1.com")
                .title("승인된 기사")
                .link("http://rss1.com/1")
                .isPublished(true)
                .build();

        News pending = News.builder()
                .sourceName("헬스경향")
                .feedUrl("http://rss2.com")
                .title("비승인 기사")
                .link("http://rss2.com/2")
                .isPublished(false)
                .build();

        newsRepository.saveAll(List.of(approved, pending));

        // when
        List<News> result = newsRepository.findAllByIsPublishedTrue();

        // then
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("승인된 기사", result.get(0).getTitle());
    }
}
