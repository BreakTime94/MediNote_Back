package com.medinote.medinote_back_kys.news.repository;

import com.medinote.medinote_back_kys.news.domain.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {

    // RSS 링크 중복 방지용 (같은 링크의 기사가 이미 저장되어 있는지 확인)
    Optional<News> findByLink(String link);

    // 관리자 승인 여부로 조회
    List<News> findAllByIsPublishedTrue();
}
