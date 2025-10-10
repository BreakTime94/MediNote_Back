package com.medinote.medinote_back_kys.news.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "news.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class NewsScheduler {

    private final NewsCollectorService newsCollectorService;

    // ✅ 진단용: 현재 설정값 확인
    @Value("${news.scheduler.enabled:true}")  // 기본값 true 지정
    private boolean enabled;

    /** 애플리케이션 시작 시 현재 설정 로그 */
    @jakarta.annotation.PostConstruct
    public void logSchedulerStatus() {
        log.info("🩵 News Scheduler enabled = {}", enabled);
    }

    /** 매일 새벽 2시(한국 시간) */
    @Scheduled(cron = "0 0 2 * * *", zone = "Asia/Seoul")
    public void collectDailyAt2am() {
        log.info("🕑 [NewsScheduler] 02:00 수집 시작");
        newsCollectorService.collectAllFeeds();
        log.info("✅ [NewsScheduler] 02:00 수집 완료");
    }
}