package com.medinote.medinote_back_kys.reply.domain.dto;

import com.medinote.medinote_back_kys.reply.domain.en.ReplyTargetType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ReplyResponseDTO(
        Long id,
        ReplyTargetType linkType,
        Long linkId,
        Long memberId,
        String title,
        String content,
        Boolean isPublic,
        LocalDateTime regDate,
        LocalDateTime modDate
) {}
