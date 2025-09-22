package com.medinote.medinote_back_kys.board.controller;

import com.medinote.medinote_back_kys.board.domain.dto.BoardCreateRequestDTO;
import com.medinote.medinote_back_kys.board.domain.dto.BoardUpdateRequestDTO;
import com.medinote.medinote_back_kys.board.service.BoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
