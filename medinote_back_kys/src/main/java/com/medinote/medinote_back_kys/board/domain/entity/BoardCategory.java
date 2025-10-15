    package com.medinote.medinote_back_kys.board.domain.entity;

    import com.medinote.medinote_back_kys.common.entity.BaseEntity;
    import jakarta.persistence.*;
    import lombok.*;

    @Entity
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @Table(name = "tbl_board_category")
    @ToString(exclude = "parent")
    public class BoardCategory extends BaseEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "category_name", nullable = false, length = 100)
        private String categoryName;

        /** 상위 카테고리 (자기참조 관계) */
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "parent_id") // FK: 상위 카테고리의 id
        private BoardCategory parent;

        /** 계층 깊이 (0=루트, 1=하위) */
        @Column(nullable = false)
        private Integer depth;

        /** 출력 순서 (선택 사항) */
        @Column(name = "sort_order")
        private Integer sortOrder;
    }
