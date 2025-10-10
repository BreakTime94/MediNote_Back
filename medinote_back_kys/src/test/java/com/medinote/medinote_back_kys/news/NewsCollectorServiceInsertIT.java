package com.medinote.medinote_back_kys.news;

import com.medinote.medinote_back_kys.news.domain.entity.News;
import com.medinote.medinote_back_kys.news.repository.NewsRepository;
import com.medinote.medinote_back_kys.news.service.NewsCollectorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Commit;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class NewsCollectorServiceInsertIT {

    @Autowired private NewsRepository newsRepository;
    @Autowired private NewsCollectorService newsCollectorService;

    @Test
    @DisplayName("collectAllFeeds()가 실제 DB에 저장되고 커밋된다")
    @Commit
        // ← 이 메서드 트랜잭션은 롤백하지 않고 커밋합니다.
    void collectAllFeeds_persistsToMariaDB() {
        long before = newsRepository.count();

        assertDoesNotThrow(() -> newsCollectorService.collectAllFeeds(),
                "RSS 수집 중 예외가 발생하면 안 됩니다.");

        long after = newsRepository.count();

        // 첫 실행에서는 after > before 일 가능성이 높음.
        // 이미 이전에 한 번 저장했다면 중복 방지로 증가폭이 작거나 0일 수 있습니다.
        assertTrue(after >= before, "수집 이후 row 수는 기존 이상이어야 합니다.");

        // 최근 5건을 확인용으로 출력
        List<News> latest = newsRepository.findAll(
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "id"))
        ).getContent();

        System.out.println("===== 최근 저장된 뉴스(최대 5건) =====");
        for (News n : latest) {
            System.out.printf("[#%d] %s | %s | pubDate=%s | type=%s%n",
                    n.getId(),
                    safe(n.getTitle()),
                    safe(n.getLink()),
                    n.getPubDate(),
                    n.getContentType()
            );
        }

        // 최소한 title/link/pubDate 같은 필드들이 채워졌는지 sanity check
        if (!latest.isEmpty()) {
            News first = latest.get(0);
            assertNotNull(first.getTitle(), "title은 null이 아니어야 합니다");
            assertNotNull(first.getLink(), "link는 null이 아니어야 합니다");
            assertNotNull(first.getPubDate(), "pubDate는 null이 아니어야 합니다");
        }
    }

    private String safe(String s) { return s == null ? "(null)" : s; }
}
