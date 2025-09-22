    package com.medinote.medinote_back_kys.board.domain.entity;

    import com.medinote.medinote_back_kys.common.entity.BaseEntity;
    import jakarta.persistence.*;
    import lombok.*;

    @Entity
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Table(name = "tbl_board_category")
    @ToString
    public class BoardCategory extends BaseEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "category_name", nullable = false)
        private String categoryName;
    }
