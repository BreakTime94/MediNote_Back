package com.medinote.medinote_back_kys.reply.controller;

import com.medinote.medinote_back_kys.common.paging.PageCriteria;
import com.medinote.medinote_back_kys.common.paging.PageResponse;
import com.medinote.medinote_back_kys.reply.domain.dto.ReplyAdminListResponseDTO;
import com.medinote.medinote_back_kys.reply.domain.dto.ReplyCreateRequestDTO;
import com.medinote.medinote_back_kys.reply.domain.dto.ReplyResponseDTO;
import com.medinote.medinote_back_kys.reply.domain.dto.ReplyUpdateRequestDTO;
import com.medinote.medinote_back_kys.reply.domain.en.ReplyTargetType;
import com.medinote.medinote_back_kys.reply.service.ReplyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/replies")
@RequiredArgsConstructor
public class ReplyContorller {

    private final ReplyService replyService;

    // ===== 댓글 등록 =====
    @PostMapping("/create")
    public ResponseEntity<ReplyResponseDTO> create(@Valid @RequestBody ReplyCreateRequestDTO dto) {
        ReplyResponseDTO response = replyService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ===== 댓글 단일 조회 =====
    @GetMapping("/detail/{id}")
    public ResponseEntity<ReplyResponseDTO> getOne(@PathVariable Long id) {
        ReplyResponseDTO response = replyService.getOne(id);
        return ResponseEntity.ok(response);
    }

    // ===== 댓글 수정 =====
    @PatchMapping("/update/{id}")
    public ResponseEntity<ReplyResponseDTO> update(@PathVariable Long id,
                                                   @Valid @RequestBody ReplyUpdateRequestDTO dto) {
        ReplyUpdateRequestDTO patchedDto = ReplyUpdateRequestDTO.builder()
                .replyId(id)
                .content(dto.content())
                .title(dto.title())
                .isPublic(dto.isPublic())
                .build();

        ReplyResponseDTO response = replyService.update(patchedDto);
        return ResponseEntity.ok(response);
    }

    // ===== 댓글 삭제 =====
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        replyService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ===== 관리자용 댓글 목록 =====
    @PostMapping("/admin/list")
    public ResponseEntity<PageResponse<ReplyAdminListResponseDTO>> getAdminList(
            @Valid @RequestBody PageCriteria criteria) {
        PageResponse<ReplyAdminListResponseDTO> response = replyService.getAdminList(criteria);
        return ResponseEntity.ok(response);
    }

    // ===== 특정 대상(Board/News)에 속한 댓글 목록 =====
    @PostMapping("/list/{targetType}/{targetId}")
    public ResponseEntity<PageResponse<ReplyAdminListResponseDTO>> getListByTarget(
            @PathVariable ReplyTargetType targetType,
            @PathVariable Long targetId,
            @Valid @RequestBody PageCriteria criteria) {

        PageResponse<ReplyAdminListResponseDTO> response =
                replyService.getListByTarget(targetType, targetId, criteria);
        return ResponseEntity.ok(response);
    }
}
