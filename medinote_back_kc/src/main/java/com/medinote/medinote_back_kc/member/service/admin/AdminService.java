package com.medinote.medinote_back_kc.member.service.admin;

import com.medinote.medinote_back_kc.member.domain.dto.admin.MemberForAdminDTO;
import com.medinote.medinote_back_kc.member.domain.dto.member.MemberDTO;

import java.util.List;

public interface AdminService {
  List<MemberForAdminDTO> getMemberList();
}
