package com.medinote.medinote_back_kys.board.controller;

import com.medinote.medinote_back_kys.board.domain.dto.*;
import com.medinote.medinote_back_kys.board.domain.en.QnaStatus;
import com.medinote.medinote_back_kys.board.service.BoardService;
import com.medinote.medinote_back_kys.common.paging.PageCriteria;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/boards")
@RequiredArgsConstructor
@Validated
@Slf4j
public class BoardController {

    private final BoardService boardService;

    /** ✅ 공통 유틸: 헤더에서 memberId 추출 */
    private Long requireMemberId(String headerMemberId) {
        if (headerMemberId == null || headerMemberId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        try {
            return Long.parseLong(headerMemberId);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "잘못된 사용자 식별자입니다.");
        }
    }

    // ===== 생성 =====
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public IdResponse create(
            @RequestHeader(value = "X-Member-Id", required = false) String headerMemberId,
            @Valid @RequestBody BoardCreateRequestDTO body
    ) {
        Long memberId = requireMemberId(headerMemberId);

        if (body.boardCategoryId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "boardCategoryId는 필수입니다.");
        }

        BoardCreateRequestDTO dto = new BoardCreateRequestDTO(
                memberId,
                body.boardCategoryId(),
                body.title(),
                body.content(),
                body.requireAdminPost(),
                body.isPublic(),
                body.qnaStatus(),
                body.postStatus()
        );

        Long id = boardService.create(dto);
        return new IdResponse(id);
    }

    // ===== 단일 조회 =====
    @GetMapping("/read/{id}")
    public BoardDetailResponseDTO get(@PathVariable @NotNull Long id) {
        return boardService.getDetail(id);
    }

    // ===== 수정 (본인만 가능) =====
    @PutMapping("/update/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(
            @PathVariable Long id,
            @RequestHeader(value = "X-Member-Id", required = false) String headerMemberId,
            @Valid @RequestBody BoardUpdateRequestDTO body
    ) {
        if (body.id() == null || !id.equals(body.id())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Path id와 body id가 일치해야 합니다.");
        }

        Long reqMemberId = requireMemberId(headerMemberId);

        log.info("[UPDATE] 요청자 memberId={}, 게시글 id={}", reqMemberId, id);
        boardService.update(body, reqMemberId);
    }

    @PutMapping("/qna/status/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateQnaStatusTemp(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        String statusStr = body.get("qnaStatus");
        if (statusStr == null || statusStr.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "qnaStatus는 필수입니다.");
        }

        QnaStatus newStatus;
        try {
            newStatus = QnaStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "유효하지 않은 상태: " + statusStr);
        }

        // ✅ 임시: 권한 체크 없이 상태만 변경
        boardService.updateQnaStatusTemp(id, newStatus);
    }

    // ===== 삭제 (본인만 가능) =====
    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id,
            @RequestHeader(value = "X-Member-Id", required = false) String headerMemberId,
            @Valid @RequestBody BoardDeleteBody body
    ) {
        Long reqMemberId = requireMemberId(headerMemberId);
        log.info("[DELETE] 요청자 memberId={}, 게시글 id={}", reqMemberId, id);

        BoardDeleteRequestDTO dto = new BoardDeleteRequestDTO(
                id,
                reqMemberId,
                body.reason()
        );

        boardService.delete(dto, reqMemberId);
    }



    // ===== 목록 =====
    @PostMapping("/notice/list")
    public BoardListResponseDTO noticeList(@Valid @RequestBody BoardListRequest req) {
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

    // ===== 내부용 간단 DTO =====
    public record IdResponse(Long id) {}
    public record BoardDeleteBody(String reason) {}
}
