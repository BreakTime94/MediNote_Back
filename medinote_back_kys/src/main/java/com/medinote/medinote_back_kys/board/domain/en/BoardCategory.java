    package com.medinote.medinote_back_kys.board.domain.en;

    public enum BoardCategory {
        NOTICE(1L), QNA(2L), FAQ(3L);
        private final Long id;
        BoardCategory(Long id) {this.id = id; }
        public Long id() {return id;}
    }
