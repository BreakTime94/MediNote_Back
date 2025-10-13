package com.medinote.medinote_back_kys.board.controller;

import com.medinote.medinote_back_kys.board.domain.dto.*;
import com.medinote.medinote_back_kys.board.service.BoardService;
import com.medinote.medinote_back_kys.common.paging.PageCriteria;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/boards")
@RequiredArgsConstructor
@Validated
public class BoardController {
    private final BoardService boardService;

    // ===== 생성 =====
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public IdResponse create(@Valid @RequestBody BoardCreateRequestDTO dto) {
        Long id = boardService.create(dto);
        return new IdResponse(id);
    }

    // ===== 단일 조회 =====
    @GetMapping("/read/{id}")
    public BoardDetailResponseDTO get(@PathVariable @NotNull Long id) {
        return boardService.getDetail(id);
    }

    // ===== 부분 수정(Patch) =====
    @PatchMapping("/update/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@PathVariable Long id, @Valid @RequestBody BoardUpdateRequestDTO dto) {
        // path id와 body id가 다르면 방어
        if (dto.id() == null || !id.equals(dto.id())) {
            throw new IllegalArgumentException("Path id와 body id가 일치해야 합니다.");
        }
        boardService.update(dto);
    }

    // ===== 소프트 삭제 =====
    // reason/requesterId를 바디로 받아 path id로 DTO 생성
    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @Valid @RequestBody BoardDeleteBody body) {
        BoardDeleteRequestDTO dto = new BoardDeleteRequestDTO(id, body.requesterId(), body.reason());
        boardService.delete(dto);
    }

    // ===== 목록(분리된 엔드포인트) =====
    @PostMapping("/notice/list")
    public BoardListResponseDTO noticeList(@Valid @RequestBody BoardListRequest req) {
        // null 방어: service에서도 한 번 더 안전망이 있으나 컨트롤러에서도 기본값 보정
        PageCriteria criteria = (req.criteria() != null) ? req.criteria() : new PageCriteria();
        return boardService.listNotice(req.cond(), criteria);
    }

    @PostMapping("/faq/list")
    public BoardListResponseDTO faqList(@Valid @RequestBody BoardListRequest req) {
        PageCriteria criteria = (req.criteria() != null) ? req.criteria() : new PageCriteria();
        return boardService.listFaq(req.cond(), criteria);
    }

    @PostMapping("/qna/list")
    public BoardListResponseDTO qnaList(@Valid @RequestBody BoardListRequest req) {
        PageCriteria criteria = (req.criteria() != null) ? req.criteria() : new PageCriteria();
        return boardService.listQna(req.cond(), criteria);
    }

    // ===== 내부용 간단 응답 DTO =====
    public record IdResponse(Long id) {}
    public record BoardDeleteBody(
            @NotNull Long requesterId,
            String reason
    ) {}
}