package com.medinote.medinote_back_kys.common.dto.member;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MemberInfoListResponse {
    private String status; // "MEMBER_INFO_LIST"
    private List<MemberInfoDTO> memberInfoList;
}
