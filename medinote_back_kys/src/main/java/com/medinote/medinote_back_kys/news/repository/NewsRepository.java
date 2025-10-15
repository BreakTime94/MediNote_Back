package com.medinote.medinote_back_kys.news.repository;

import com.medinote.medinote_back_kys.news.domain.en.ContentType;
import com.medinote.medinote_back_kys.news.domain.entity.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NewsRepository extends JpaRepository<News, Long>, JpaSpecificationExecutor<News> {

    // ===== 중복 방지 / 기본 조회 =====
    Optional<News> findByLink(String link);
    boolean existsByLink(String link);

    // ===== ✅ 공개 목록 (사용자용) =====
    Page<News> findByIsPublishedTrue(Pageable pageable);

    Page<News> findByIsPublishedTrueAndContentType(ContentType contentType, Pageable pageable);

    // 🔸 (NEW) ContentType이 null일 경우 전체 + Published만 필터링
    @Query("""
        select n from News n
        where n.isPublished = true
          and (:contentType is null or n.contentType = :contentType)
        """)
    Page<News> findPublicList(@Param("contentType") ContentType contentType, Pageable pageable);

    // 🔸 (NEW) 키워드 기반 공개 검색
    @Query("""
        select n from News n
        where n.isPublished = true
          and (
               lower(n.title)       like lower(concat('%', :keyword, '%'))
            or lower(n.description) like lower(concat('%', :keyword, '%'))
            or lower(n.author)      like lower(concat('%', :keyword, '%'))
          )
        """)
    Page<News> searchPublic(@Param("keyword") String keyword, Pageable pageable);

    // ===== 무한스크롤 (Slice) =====
    Slice<News> findSliceByIsPublishedTrueAndContentTypeOrderByPubDateDesc(ContentType contentType, Pageable pageable);

    // ===== 관리자 목록 / 검색 =====
    Page<News> findByContentType(ContentType contentType, Pageable pageable);
    Page<News> findByContentTypeAndIsPublished(ContentType contentType, boolean isPublished, Pageable pageable);

    @Query("""
        select n from News n
        where (:contentType is null or n.contentType = :contentType)
          and (:published   is null or n.isPublished = :published)
          and (
                :keyword is null
             or lower(n.title)       like lower(concat('%', :keyword, '%'))
             or lower(n.description) like lower(concat('%', :keyword, '%'))
             or lower(n.author)      like lower(concat('%', :keyword, '%'))
          )
        """)
    Page<News> searchAdmin(@Param("contentType") ContentType contentType,
                           @Param("published")   Boolean published,
                           @Param("keyword")     String keyword,
                           Pageable pageable);

    // ===== 기간 필터 / 배치용 =====
    Page<News> findByRegDateBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);

    // ===== 발행상태 일괄변경 =====
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update News n set n.isPublished = :published where n.id in :ids")
    int updatePublishStatus(@Param("ids") List<Long> ids, @Param("published") boolean published);

    // ===== 통계 / 모니터링 =====
    long countByIsPublishedTrue();
    long countByContentTypeAndIsPublished(ContentType contentType, boolean isPublished);

    Optional<News> findTop1ByContentTypeAndIsPublishedTrueOrderByPubDateDesc(ContentType contentType);

    boolean isPublished(Boolean isPublished);
}
