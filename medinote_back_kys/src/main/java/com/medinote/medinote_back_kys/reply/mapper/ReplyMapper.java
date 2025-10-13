package com.medinote.medinote_back_kys.reply.mapper;

import com.medinote.medinote_back_kys.reply.domain.dto.ReplyAdminListResponseDTO;
import com.medinote.medinote_back_kys.reply.domain.dto.ReplyCreateRequestDTO;
import com.medinote.medinote_back_kys.reply.domain.dto.ReplyResponseDTO;
import com.medinote.medinote_back_kys.reply.domain.entity.Reply;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReplyMapper {

    // === Create DTO → Entity ===
    @Mapping(target = "id", ignore = true)
    Reply toEntity(ReplyCreateRequestDTO dto);

    // === Entity → ResponseDTO ===
    ReplyResponseDTO toResponseDto(Reply entity);

    // === Entity → Admin ResponseDTO ===
    @Mapping(target = "memberNickname", ignore = true)
    ReplyAdminListResponseDTO toAdminResponseDto(Reply entity);
}
