package com.medinote.medinote_back_kys.common.dto.member;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberInfoDTO {

    private Long id;
    private String nickname;
    private String role; // "ADMIN" | "USER"
}
