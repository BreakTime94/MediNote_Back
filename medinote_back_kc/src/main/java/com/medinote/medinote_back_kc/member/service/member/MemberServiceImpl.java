package com.medinote.medinote_back_kc.member.service.member;

import com.medinote.medinote_back_kc.member.domain.dto.member.MemberDTO;
import com.medinote.medinote_back_kc.member.domain.dto.member.RegisterRequestDTO;
import com.medinote.medinote_back_kc.member.domain.dto.member.UpdateRequestDTO;
import com.medinote.medinote_back_kc.member.domain.entity.member.Member;
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
  public void register(RegisterRequestDTO dto) {
    Member member = mapper.toRegister(dto, passwordEncoder);
    repository.save(member);
  }

  @Override
  public MemberDTO get(String email) {
    return mapper.toMemberDTO(repository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("email을 확인하여 주시기 바랍니다.")));
  }

  @Override
  public void update(UpdateRequestDTO dto, Long id) {
    Member member = (repository.findById(id).orElseThrow(()-> new UsernameNotFoundException("email을 확인하여 주시기 바랍니다.")));
    member.changeMyPage(dto);
    repository.save(member);
  }

  @Override
  @Transactional
  public void delete(String email) {
    repository.softDelete(email);
  }
}
