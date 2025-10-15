package com.medinote.medinote_back_kc.member.service.admin;

import com.medinote.medinote_back_kc.member.domain.dto.admin.MemberForAdminDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {

  @Override
  public List<MemberForAdminDTO> getMemberList() {
    return List.of();
  }
}
