package com.medinote.medinote_back_kys.board.controller;

import com.medinote.medinote_back_kys.board.domain.dto.BoardCreateRequestDTO;
import com.medinote.medinote_back_kys.board.domain.dto.BoardDetailResponseDTO;
import com.medinote.medinote_back_kys.board.domain.dto.BoardListResponseDTO;
import com.medinote.medinote_back_kys.board.domain.dto.BoardUpdateRequestDTO;
import com.medinote.medinote_back_kys.board.service.BoardService;
import com.medinote.medinote_back_kys.common.paging.Criteria;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "http://localhost:6006")
@RestController
@RequestMapping("/boards")
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;

    @PostMapping(consumes = "application/json")
    public ResponseEntity<String> createBoard(@Valid @RequestBody BoardCreateRequestDTO dto) {
        boardService.createBoard(dto);
        return ResponseEntity.ok("OK");
    }

    @PutMapping(consumes = "application/json")
    public ResponseEntity<String> updateBoard(@Valid @RequestBody BoardUpdateRequestDTO dto) {
        boardService.updateBoard(dto);
        return ResponseEntity.ok("UPDATED");
    }

    @GetMapping
    public ResponseEntity<BoardListResponseDTO> listBoards(@ModelAttribute Criteria criteria) {
        // 1) 정렬 화이트리스트(클라이언트 필드 → 실제 엔티티 프로퍼티)
        Map<String, String> whitelist = Map.of(
                "id", "id",
                "title", "title",
                "regDate", "regDate",
                "postStatus", "postStatus",
                "qnaStatus", "qnaStatus"
        );

        // 2) 1차 조회
        BoardListResponseDTO dto = boardService.listBoards(criteria, whitelist);

        // 3) 요청 페이지가 전체 페이지를 초과했다면 마지막 페이지로 보정 후 재조회
        int totalPages = dto.getPage().getTotalPages(); // 0일 수도 있음(데이터 없음)
        if (totalPages > 0 && criteria.getPage() > totalPages) {
            criteria.setPage(totalPages); // 마지막 페이지로 이동
            dto = boardService.listBoards(criteria, whitelist);
        }

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BoardDetailResponseDTO> getBoard(@PathVariable Long id) {
        return ResponseEntity.ok(boardService.getBoard(id));
    }

    @DeleteMapping(consumes = "application/json")
    public ResponseEntity<String> deleteBoard(@Valid @RequestBody com.medinote.medinote_back_kys.board.domain.dto.BoardDeleteRequestDTO dto) {
        boardService.deletedBoard(dto);
        return ResponseEntity.ok("DELETED");
    }
}
