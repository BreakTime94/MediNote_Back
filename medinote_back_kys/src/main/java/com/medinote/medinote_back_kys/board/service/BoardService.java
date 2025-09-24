package com.medinote.medinote_back_kys.board.service;

import com.medinote.medinote_back_kys.board.domain.dto.BoardCreateRequestDTO;
import com.medinote.medinote_back_kys.board.domain.dto.BoardDetailResponseDTO;
import com.medinote.medinote_back_kys.board.domain.dto.BoardListResponseDTO;
import com.medinote.medinote_back_kys.board.domain.dto.BoardUpdateRequestDTO;
import com.medinote.medinote_back_kys.board.domain.en.PostStatus;
import com.medinote.medinote_back_kys.board.domain.entity.Board;
import com.medinote.medinote_back_kys.board.mapper.BoardMapper;
import com.medinote.medinote_back_kys.board.repository.BoardRepository;
import com.medinote.medinote_back_kys.common.paging.Criteria;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

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

    public BoardListResponseDTO listBoards(Criteria criteria) {
        // 테스트에서 사용한 화이트리스트와 동일한 매핑
        Map<String, String> whitelist = Map.of(
                "id", "id",
                "title", "title",
                "regDate", "regDate",
                "postStatus", "postStatus",
                "qnaStatus", "qnaStatus"
        );

        Pageable pageable = criteria.toPageable(whitelist);
        Page<Board> page = boardRepository.findAll(pageable);

        // 매퍼로 페이지 메타 + 아이템 매핑
        return boardMapper.toListResponse(page, criteria);
    }

    /**
     * 목록 조회(확장 버전): 호출 측에서 화이트리스트(클라이언트 필드 → 실제 프로퍼티) 지정 가능.
     */
    public BoardListResponseDTO listBoards(Criteria criteria, Map<String, String> whitelist) {
        Pageable pageable = criteria.toPageable(whitelist);
        Page<Board> page = boardRepository.findAll(pageable);
        return boardMapper.toListResponse(page, criteria);
    }


    //단일조회
    public BoardDetailResponseDTO getBoard(Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found: " + id));

        // (선택) 접근 제어 필요 시 여기서 체크
        // if (!board.getIsPublic()) { ... 권한 체크 ... }
        // if (board.getPostStatus() == PostStatus.DELETED) { throw new ResourceNotFoundException(...); }
        //접근제한 삭제된 글
        if (board.getPostStatus() == PostStatus.DELETED) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found: " + id);
        }

        return boardMapper.toDetailResponse(board);
    }
}
