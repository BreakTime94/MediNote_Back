package com.medinote.medinote_back_kys.board.service;

import com.medinote.medinote_back_kys.board.domain.dto.BoardCreateRequestDTO;
import com.medinote.medinote_back_kys.board.domain.entity.Board;
import com.medinote.medinote_back_kys.board.mapper.BoardMapper;
import com.medinote.medinote_back_kys.board.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {
    private final BoardRepository  boardRepository;
    private final BoardMapper boardMapper;

    @Transactional
    public Board createBoard(BoardCreateRequestDTO dto){
        //1. 추후 검증 로직 추가

        //2.
        Board entity = boardMapper.toEntity(dto);

        return boardRepository.save(entity);
    }


}
