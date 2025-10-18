package com.medinote.medinote_back_kys.common.client;

import com.medinote.medinote_back_kys.common.dto.member.MemberInfoDTO;
import com.medinote.medinote_back_kys.common.dto.member.MemberInfoListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class MemberClient {

    private final RestTemplate restTemplate;

    @Value("${member.service.base-url}")
    private String baseUrl;

    // 매우 단순한 5분 캐시
    private final Map<Long, MemberInfoDTO> cache = new ConcurrentHashMap<>();
    private volatile Instant lastLoaded = Instant.EPOCH;
    private static final long TTL_SECONDS = 300;

    public Optional<MemberInfoDTO> getById(Long id) {
        ensureCache();
        return Optional.ofNullable(cache.get(id));
    }

    private synchronized void ensureCache() {
        if (Instant.now().minusSeconds(TTL_SECONDS).isBefore(lastLoaded)) return;

        String url = baseUrl + "/member/list/info";
        MemberInfoListResponse resp =
                restTemplate.getForObject(url, MemberInfoListResponse.class);

        cache.clear();
        if (resp != null && resp.getMemberInfoList() != null) {
            for (MemberInfoDTO m : resp.getMemberInfoList()) {
                cache.put(m.getId(), m);
            }
        }
        lastLoaded = Instant.now();
    }
}
