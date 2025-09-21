package com.medinote.medinote_back_kys.board.service;

import com.medinote.medinote_back_kys.board.domain.dto.BoardCreateRequestDTO;
import com.medinote.medinote_back_kys.board.domain.dto.BoardUpdateRequestDTO;
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
    public Board createBoard(BoardCreateRequestDTO dto) {
        // DTO → Entity 변환
        Board entity = boardMapper.toEntity(dto);

        // 저장
        return boardRepository.save(entity);
    }

    @Transactional
    public Board updateBoard(BoardUpdateRequestDTO dto) {
        // 1) 대상 엔티티 조회 (없으면 404/400 성격 예외)
        Board entity = boardRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다. id=" + dto.getId()));

        // 2) 부분 업데이트 (null 필드는 무시)
        //    BoardMapper#updateEntityFromDto 가
        //    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE) 로 설정되어 있어
        //    dto에서 null이 아닌 값만 entity에 반영됩니다.
        boardMapper.updateEntityFromDto(dto, entity);

        return entity;
    }

}
