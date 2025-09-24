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

    //조회용
    //JpaSpecificationExecutor는 findAll(Specification, Pageable)를 제공함

    private BoardSpecs() {}

    public static Specification<Board> isPublicTrue() {
        return (root, q, cb) -> cb.isTrue(root.get("isPublic"));
    }

    public static Specification<Board> categoryEquals(Long categoryId) {
        if (categoryId == null) return null;
        return (root, q, cb) -> cb.equal(root.get("boardCategoryId"), categoryId);
    }

    /** 제목+내용 LIKE 검색 (간단 버전, 필요시 lower/escape 처리 확장) */
    public static Specification<Board> keywordLike(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;
        final String like = "%" + keyword.trim() + "%";

        return (root, q, cb) -> {
            // title은 VARCHAR 라면 lower 사용 가능하지만, 굳이 안 써도 됨
            var pTitle = cb.like(root.get("title"), like);     // ci collation이면 대소문자 무시

            // content가 TEXT(@Lob/text/longtext)라면 lower() 적용 금지
            var pContent = cb.like(root.get("content"), like); // 그대로 LIKE

            return cb.or(pTitle, pContent);
        };
    }

    public static Specification<Board> qnaStatusEquals(QnaStatus status) {
        if (status == null) return null;
        return (root, q, cb) -> cb.and(
                cb.equal(root.get("boardCategoryId"), 2L),
                cb.equal(root.get("qnaStatus"), status)
        );}

    public static Specification<Board> writerEquals(Long writerId) {
        if (writerId == null) return null;
        return (root, q, cb) -> cb.equal(root.get("memberId"), writerId);
    }

    public static Specification<Board> requireAdminPost(Boolean require) {
        if (require == null) return null;
        return (root, q, cb) -> cb.equal(root.get("requireAdminPost"), require);
    }

    public static Specification<Board> statusIn(Set<PostStatus> statuses) {
        if (statuses == null || statuses.isEmpty()) return null;
        return (root, q, cb) -> root.get("postStatus").in(statuses);
    }

    /** [from, to) 반열림 구간 */
    public static Specification<Board> regBetween(LocalDateTime from, LocalDateTime to) {
        if (from == null && to == null) return null;
        return (root, q, cb) -> {
            Path<LocalDateTime> reg = root.get("regDate");
            if (from != null && to != null) return cb.between(reg, from, to); // inclusive, 필요시 < 로 바꿔도 됨
            if (from != null) return cb.greaterThanOrEqualTo(reg, from);
            return cb.lessThan(reg, to); // [ , to)
        };
    }

    /** 유저 목록용 베이스 정책: 공개글 + (PUBLISHED/HIDDEN) */
    public static Specification<Board> userVisibleBaseline() {
        return Specification.allOf(
                isPublicTrue(),
                statusIn(EnumSet.of(PostStatus.PUBLISHED, PostStatus.HIDDEN))
        );
    }


}
