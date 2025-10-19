package com.medinote.medinote_back_kys.reply.service;

import com.medinote.medinote_back_kys.common.paging.PageCriteria;
import com.medinote.medinote_back_kys.common.paging.PageResponse;
import com.medinote.medinote_back_kys.reply.domain.dto.ReplyAdminListResponseDTO;
import com.medinote.medinote_back_kys.reply.domain.dto.ReplyCreateRequestDTO;
import com.medinote.medinote_back_kys.reply.domain.dto.ReplyResponseDTO;
import com.medinote.medinote_back_kys.reply.domain.dto.ReplyUpdateRequestDTO;
import com.medinote.medinote_back_kys.reply.domain.en.ReplyTargetType;
import com.medinote.medinote_back_kys.reply.domain.entity.Reply;
import com.medinote.medinote_back_kys.reply.mapper.ReplyMapper;
import com.medinote.medinote_back_kys.reply.repository.ReplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReplyService {

    private final ReplyRepository replyRepository;
    private final ReplyMapper replyMapper;

    // ===== 댓글 등록 =====
    @Transactional
    public ReplyResponseDTO create(ReplyCreateRequestDTO dto) {
        Reply entity = replyMapper.toEntity(dto);

        // 공개 여부가 null이면 기본 true 처리
        if (entity.getIsPublic() == null) {
            entity = Reply.builder()
                    .id(entity.getId())
                    .linkType(entity.getLinkType())
                    .linkId(entity.getLinkId())
                    .memberId(entity.getMemberId())
                    .title(entity.getTitle())
                    .content(entity.getContent())
                    .isPublic(Boolean.TRUE)
                    .build();
        }

        Reply saved = replyRepository.save(entity);
        return replyMapper.toResponseDto(saved);
    }

    // ===== 댓글 수정 =====
    @Transactional
    public ReplyResponseDTO update(ReplyUpdateRequestDTO dto) {
        Reply reply = replyRepository.findById(dto.replyId())
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다. id=" + dto.replyId()));

        if (dto.content() != null) {
            reply.changeContent(dto.content());
        }
        if (dto.title() != null) {
            reply.changeTitle(dto.title());
        }
        if (dto.isPublic() != null) {
            reply.changeVisibility(dto.isPublic());
        }

        return replyMapper.toResponseDto(reply);
    }

    // ===== 댓글 삭제 =====
    @Transactional
    public void delete(Long replyId) {
        if (!replyRepository.existsById(replyId)) {
            throw new IllegalArgumentException("삭제할 댓글이 존재하지 않습니다. id=" + replyId);
        }
        replyRepository.deleteById(replyId);
    }

    // ===== 단일 댓글 조회 =====
    public ReplyResponseDTO getOne(Long replyId) {
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다. id=" + replyId));
        return replyMapper.toResponseDto(reply);
    }

    // ===== 관리자용 댓글 목록 조회 (전체) =====
    public PageResponse<ReplyAdminListResponseDTO> getAdminList(PageCriteria criteria) {
        Map<String, String> whitelist = Map.of(
                "id", "id",
                "regDate", "regDate",
                "modDate", "modDate"
        );

        Pageable pageable = criteria.toPageable(whitelist);
        Page<Reply> page = replyRepository.findAll(pageable);

        List<ReplyAdminListResponseDTO> dtoList = page.getContent().stream()
                .map(replyMapper::toAdminResponseDto)
                .toList();

        return new PageResponse<>(
                dtoList,
                criteria.toPageMeta(page.getTotalElements()),
                criteria.getSort(),
                null,
                criteria.getFilters()
        );
    }

    // ===== 특정 대상(Board/News)에 속한 댓글 목록 조회 =====
    public PageResponse<ReplyAdminListResponseDTO> getListByTarget(ReplyTargetType linkType, Long linkId, PageCriteria criteria) {
        Map<String, String> whitelist = Map.of(
                "id", "id",
                "regDate", "regDate",
                "modDate", "modDate"
        );

        Pageable pageable = criteria.toPageable(whitelist);
        Page<Reply> page = replyRepository.findByLinkTypeAndLinkId(linkType, linkId, pageable);

        List<ReplyAdminListResponseDTO> dtoList = page.getContent().stream()
                .map(replyMapper::toAdminResponseDto)
                .toList();

        return new PageResponse<>(
                dtoList,
                criteria.toPageMeta(page.getTotalElements()),
                criteria.getSort(),
                null,
                criteria.getFilters()
        );
    }
}
