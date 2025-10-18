package com.medinote.medinote_back_kys.board.repository;

import com.medinote.medinote_back_kys.board.domain.en.PostStatus;
import com.medinote.medinote_back_kys.board.domain.en.QnaStatus;
import com.medinote.medinote_back_kys.board.domain.entity.Board;
import com.medinote.medinote_back_kys.board.domain.entity.BoardCategory;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;

/**
 * Board 엔티티용 Specification 모음
 * - null 입력 시 자동으로 skip되도록 설계
 */
public class BoardSpecs {

    private BoardSpecs() {}

    /** ✅ 공개 여부 = true */
    public static Specification<Board> isPublicTrue() {
        return (root, q, cb) -> cb.isTrue(root.get("isPublic"));
    }

    /** ✅ 단일 카테고리 ID 일치 */
    public static Specification<Board> categoryEquals(Long categoryId) {
        if (categoryId == null) return null;
        return (root, q, cb) -> cb.equal(root.get("boardCategoryId"), categoryId);
    }

    /** ✅ 다중 카테고리 IN */
    public static Specification<Board> categoryIn(Set<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) return null;
        return (root, q, cb) -> root.get("boardCategoryId").in(categoryIds);
    }

    /**
     * ✅ 상위 카테고리(rootId) 자신 + 직계 자식 포함 조회
     * - 현재 스키마 depth=1 기준 Subquery 활용
     */
    public static Specification<Board> categoryRootOrChild(Long rootId) {
        if (rootId == null) return null;
        return (root, q, cb) -> {
            Subquery<Long> sub = q.subquery(Long.class);
            Root<BoardCategory> c = sub.from(BoardCategory.class);
            sub.select(c.get("id"))
                    .where(cb.equal(c.get("parent").get("id"), rootId));

            return cb.or(
                    cb.equal(root.get("boardCategoryId"), rootId),
                    root.get("boardCategoryId").in(sub)
            );
        };
    }

    /**
     * ✅ 제목 + 내용 LIKE 검색
     * - 키워드 양쪽에 '%' 자동 추가
     * - 제목 또는 내용 중 하나라도 일치하면 포함
     */
    public static Specification<Board> keywordLike(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;
        final String like = "%" + keyword.trim() + "%";

        return (root, q, cb) -> cb.or(
                cb.like(root.get("title"), like),
                cb.like(root.get("content"), like)
        );
    }

    /** ✅ QnA 상태 일치 */
    public static Specification<Board> qnaStatusEquals(QnaStatus status) {
        if (status == null) return null;
        return (root, q, cb) -> cb.equal(root.get("qnaStatus"), status);
    }

    /** ✅ 작성자 ID 일치 */
    public static Specification<Board> writerEquals(Long writerId) {
        if (writerId == null) return null;
        return (root, q, cb) -> cb.equal(root.get("memberId"), writerId);
    }

    /** ✅ 관리자 전용 여부 */
    public static Specification<Board> requireAdminPost(Boolean require) {
        if (require == null) return null;
        return (root, q, cb) -> cb.equal(root.get("requireAdminPost"), require);
    }

    /** ✅ 상태 집합 포함 여부 */
    public static Specification<Board> statusIn(Set<PostStatus> statuses) {
        if (statuses == null || statuses.isEmpty()) return null;
        return (root, q, cb) -> root.get("postStatus").in(statuses);
    }

    /** ✅ 등록일 범위 [from, to) */
    public static Specification<Board> regBetween(LocalDateTime from, LocalDateTime to) {
        if (from == null && to == null) return null;
        return (root, q, cb) -> {
            Path<LocalDateTime> reg = root.get("regDate");
            if (from != null && to != null) return cb.between(reg, from, to);
            if (from != null) return cb.greaterThanOrEqualTo(reg, from);
            return cb.lessThan(reg, to);
        };
    }

    /** ✅ 사용자 조회용 기본 조건: 공개 + (PUBLISHED or HIDDEN) */
    public static Specification<Board> userVisibleBaseline() {
        return Specification.allOf(
                isPublicTrue(),
                statusIn(EnumSet.of(PostStatus.PUBLISHED, PostStatus.HIDDEN))
        );
    }
}
