package com.medinote.medinote_back_kc.member.service;

import com.medinote.medinote_back_kc.member.domain.dto.MemberResponseDTO;
import com.medinote.medinote_back_kc.member.domain.dto.RegisterRequestDTO;
import com.medinote.medinote_back_kc.member.domain.entity.Member;
import com.medinote.medinote_back_kc.member.mapper.MemberMapper;
import com.medinote.medinote_back_kc.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class MemberServiceImpl implements MemberService{
  @Autowired
  private MemberRepository repository;
  @Autowired
  private MemberMapper mapper;
  @Autowired
  private PasswordEncoder passwordEncoder;

  @Override
  public Long register(RegisterRequestDTO dto) {
    Member member = mapper.toMember(dto, passwordEncoder);
    return repository.save(member).getId();
  }

  @Override
  public MemberResponseDTO get(Long id) {
    return mapper.toMemberDTO(repository.findById(id).orElseThrow(() -> new UsernameNotFoundException("id를 확인하여 주시기 바랍니다.")));
  }

  @Override
  public void update(MemberResponseDTO dto) {

  }

  @Override
  @Transactional
  public void delete(String email) {
    repository.softDelete(email);
  }
}
