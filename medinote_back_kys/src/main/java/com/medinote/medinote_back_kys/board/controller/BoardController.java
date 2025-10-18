package com.medinote.medinote_back_kys.board.controller;

import com.medinote.medinote_back_kys.board.domain.dto.*;
import com.medinote.medinote_back_kys.board.service.BoardService;
import com.medinote.medinote_back_kys.common.paging.PageCriteria;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/boards")
@RequiredArgsConstructor
@Validated
@Slf4j
public class BoardController {

    private final BoardService boardService;

    // ===== 카테고리 상수 (프로젝트 실제 값에 맞춰 조정하세요) =====
    private static final long CAT_NOTICE = 1L; // 공지
    private static final long CAT_QNA    = 2L; // QnA
    private static final long CAT_FAQ    = 3L; // FAQ

    // ====== 공통 유틸 ======
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

    private boolean isAdmin(String headerRole) {
        return headerRole != null && headerRole.equalsIgnoreCase("ADMIN");
    }

    private boolean isNoticeOrFaq(long categoryId) {
        return categoryId == CAT_NOTICE || categoryId == CAT_FAQ;
    }

    // ===== 생성 =====
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public IdResponse create(
            @RequestHeader(value = "X-Member-Id", required = false) String headerMemberId,
            @RequestHeader(value = "X-Member-Role", required = false) String headerRole,
            @Valid @RequestBody BoardCreateRequestDTO body  // @Valid 유지
    ) {
        Long memberId = requireMemberId(headerMemberId);

        // 검증
        if (body.boardCategoryId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "boardCategoryId는 필수입니다.");
        }

        // 새 DTO 생성 (memberId 주입)
        BoardCreateRequestDTO dto = new BoardCreateRequestDTO(
                memberId,  // 헤더에서 온 값
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

    // ===== 수정(전체/부분) =====
    @PutMapping("/update/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(
            @PathVariable Long id,
            @RequestHeader(value = "X-Member-Id",   required = false) String headerMemberId,
            @RequestHeader(value = "X-Member-Role", required = false) String headerRole,
            @RequestHeader(value = "X-Role",        required = false) String headerRoleAlt, // 보조키
            @Valid @RequestBody BoardUpdateRequestDTO body
    ) {
        final String effectiveRole = (headerRole != null) ? headerRole : headerRoleAlt;

        // ★ 임시 로그: 들어온 헤더와 효과적 역할값
        log.info("[UPDATE] id={}, X-Member-Id={}, X-Member-Role={}, X-Role={}, effectiveRole={}",
                id, headerMemberId, headerRole, headerRoleAlt, effectiveRole);

        if (body.id() == null || !id.equals(body.id())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Path id와 body id가 일치해야 합니다.");
        }

        Long reqMemberId = requireMemberId(headerMemberId);

        BoardDetailResponseDTO origin = boardService.getDetail(id);
        if (origin == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다.");

        Long mergedCategoryId = (body.boardCategoryId() != null) ? body.boardCategoryId() : origin.boardCategoryId();
        if (mergedCategoryId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "boardCategoryId가 필요합니다.");
        }

        // ★ 임시 로그: 권한 판정에 필요한 핵심 값들
        log.info("[UPDATE] origin.memberId={}, origin.categoryId={}, merged.categoryId={}, requesterId={}",
                origin.memberId(), origin.boardCategoryId(), mergedCategoryId, reqMemberId);

        if (isNoticeOrFaq(mergedCategoryId)) {
            if (!isAdmin(effectiveRole)) {
                log.info("[UPDATE] forbidden: not admin. effectiveRole={}", effectiveRole); // ★
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자만 수정할 수 있습니다.");
            }
        } else if (mergedCategoryId == CAT_QNA) {
            if (!isAdmin(effectiveRole) && !reqMemberId.equals(origin.memberId())) {
                log.info("[UPDATE] forbidden: not owner/admin. requesterId={}, owner={}", reqMemberId, origin.memberId()); // ★
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "작성자 또는 관리자만 수정할 수 있습니다.");
            }
        }

        BoardUpdateRequestDTO secured = new BoardUpdateRequestDTO(
                id,
                mergedCategoryId,
                (body.title() != null)   ? body.title()   : origin.title(),
                (body.content() != null) ? body.content() : origin.content(),
                (body.isPublic() != null) ? body.isPublic()
                        : (origin.isPublic() != null ? origin.isPublic() : true),
                (body.requireAdminPost() != null) ? body.requireAdminPost()
                        : (origin.requireAdminPost() != null ? origin.requireAdminPost() : false),
                (body.qnaStatus() != null)  ? body.qnaStatus()  : origin.qnaStatus(),
                (body.postStatus() != null) ? body.postStatus() : origin.postStatus()
        );

        boardService.update(secured);
        log.info("[UPDATE] done. id={}", id); // ★ 결과 로그
    }

    // ===== 소프트 삭제 =====
    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id,
            @RequestHeader(value = "X-Member-Id",   required = false) String headerMemberId,
            @RequestHeader(value = "X-Member-Role", required = false) String headerRole,
            @Valid @RequestBody BoardDeleteBody body // body.requesterId는 무시됨
    ) {
        Long reqMemberId = requireMemberId(headerMemberId);

        BoardDetailResponseDTO origin = boardService.getDetail(id);
        if (origin == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다.");
        }

        Long catId = origin.boardCategoryId();
        if (catId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "boardCategoryId가 필요합니다.");
        }

        // 권한 검사
        if (isNoticeOrFaq(catId)) {
            // 공지/FAQ 삭제는 관리자만
            if (!isAdmin(headerRole)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자만 삭제할 수 있습니다.");
            }
        } else if (catId == CAT_QNA) {
            // QnA 삭제는 작성자 본인 또는 관리자
            if (!isAdmin(headerRole) && !reqMemberId.equals(origin.memberId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "작성자 또는 관리자만 삭제할 수 있습니다.");
            }
        }

        // 게이트웨이 헤더의 memberId를 requesterId로 사용
        BoardDeleteRequestDTO dto = new BoardDeleteRequestDTO(
                id,
                reqMemberId,
                body.reason()
        );
        boardService.delete(dto);
    }

    // ===== 목록(분리된 엔드포인트) =====
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

    // ===== 내부용 간단 응답 DTO =====
    public record IdResponse(Long id) {}
    public record BoardDeleteBody(
            @NotNull Long requesterId, // [참고] 게이트웨이 헤더를 사용하므로 컨트롤러에서는 값 무시 가능
            String reason
    ) {}
}
