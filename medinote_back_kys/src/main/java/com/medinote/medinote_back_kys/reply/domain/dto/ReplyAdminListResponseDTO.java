package com.medinote.medinote_back_kys.reply.domain.dto;

import com.medinote.medinote_back_kys.reply.domain.en.ReplyTargetType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ReplyAdminListResponseDTO(
        Long id,
        ReplyTargetType linkType,
        Long linkId,
        Long memberId,
        String memberNickname,       // 관리자용 추가 정보
        String title,
        String content,
        Boolean isPublic,
        LocalDateTime regDate,
        LocalDateTime modDate
) {}
