package com.medinote.medinote_back_kys.board.repository;

import com.medinote.medinote_back_kys.board.domain.en.PostStatus;
import com.medinote.medinote_back_kys.board.domain.en.QnaStatus;
import com.medinote.medinote_back_kys.board.domain.entity.Board;
import jakarta.persistence.criteria.Path;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;

public class BoardSpecs {

    private BoardSpecs() {}

    /** 공개 여부 = true */
    public static Specification<Board> isPublicTrue() {
        return (root, q, cb) -> cb.isTrue(root.get("isPublic"));
    }

    /** 카테고리 ID 일치 */
    public static Specification<Board> categoryEquals(Long categoryId) {
        if (categoryId == null) return null;
        return (root, q, cb) -> cb.equal(root.get("boardCategoryId"), categoryId);
    }

    /** 제목+내용 LIKE 검색 */
    public static Specification<Board> keywordLike(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;
        final String like = "%" + keyword.trim() + "%";

        return (root, q, cb) -> {
            var pTitle   = cb.like(root.get("title"), like);
            var pContent = cb.like(root.get("content"), like);
            return cb.or(pTitle, pContent);
        };
    }

    /** QnA 상태 일치 */
    public static Specification<Board> qnaStatusEquals(QnaStatus status) {
        if (status == null) return null;
        return (root, q, cb) -> cb.equal(root.get("qnaStatus"), status);
    }

    /** 작성자 ID 일치 */
    public static Specification<Board> writerEquals(Long writerId) {
        if (writerId == null) return null;
        return (root, q, cb) -> cb.equal(root.get("memberId"), writerId);
    }

    /** 관리자 전용 글 여부 */
    public static Specification<Board> requireAdminPost(Boolean require) {
        if (require == null) return null;
        return (root, q, cb) -> cb.equal(root.get("requireAdminPost"), require);
    }

    /** 상태 집합 포함 여부 */
    public static Specification<Board> statusIn(Set<PostStatus> statuses) {
        if (statuses == null || statuses.isEmpty()) return null;
        return (root, q, cb) -> root.get("postStatus").in(statuses);
    }

    /** 등록일 범위 [from, to) */
    public static Specification<Board> regBetween(LocalDateTime from, LocalDateTime to) {
        if (from == null && to == null) return null;
        return (root, q, cb) -> {
            Path<LocalDateTime> reg = root.get("regDate");
            if (from != null && to != null) return cb.between(reg, from, to);
            if (from != null) return cb.greaterThanOrEqualTo(reg, from);
            return cb.lessThan(reg, to);
        };
    }

    /** 유저 조회용 기본 정책: 공개글 + PUBLISHED/HIDDEN */
    public static Specification<Board> userVisibleBaseline() {
        return Specification.allOf(
                isPublicTrue(),
                statusIn(EnumSet.of(PostStatus.PUBLISHED, PostStatus.HIDDEN))
        );
    }
}