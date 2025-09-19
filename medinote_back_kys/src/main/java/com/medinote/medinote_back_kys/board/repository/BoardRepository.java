package com.medinote.medinote_back_kys.board.repository;

import com.medinote.medinote_back_kys.board.domain.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long> {
}
