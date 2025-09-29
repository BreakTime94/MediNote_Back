package com.medinote.medinote_back_kc.member.service.member;

import com.medinote.medinote_back_kc.common.exception.DuplicateEmailException;
import com.medinote.medinote_back_kc.common.exception.DuplicateNicknameException;
import com.medinote.medinote_back_kc.member.domain.dto.member.MemberDTO;
import com.medinote.medinote_back_kc.member.domain.dto.member.RegisterRequestDTO;
import com.medinote.medinote_back_kc.member.domain.dto.member.UpdateRequestDTO;
import com.medinote.medinote_back_kc.member.domain.entity.member.Member;
import com.medinote.medinote_back_kc.member.mapper.MemberMapper;
import com.medinote.medinote_back_kc.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService{

  private final MemberRepository repository;
  private final MemberMapper mapper;
  private final PasswordEncoder passwordEncoder;

  @Override
  public void register(RegisterRequestDTO dto) {

    if(repository.existsByEmail(dto.getEmail())) {
      throw new DuplicateEmailException("이미 등록된 이메일입니다.");
    }

    if(repository.existsByNickname(dto.getNickname())) {
      throw new DuplicateNicknameException("이미 등록된 닉네임입니다.");
    }

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
