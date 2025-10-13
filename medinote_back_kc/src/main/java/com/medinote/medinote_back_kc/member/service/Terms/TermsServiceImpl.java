package com.medinote.medinote_back_kc.member.service.Terms;

import com.medinote.medinote_back_kc.member.domain.dto.terms.TermsDTO;
import com.medinote.medinote_back_kc.member.domain.entity.terms.Terms;
import com.medinote.medinote_back_kc.member.mapper.TermsMapper;
import com.medinote.medinote_back_kc.member.repository.TermsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class TermsServiceImpl implements TermsService {
  private final TermsRepository termsRepository;
  private final TermsMapper termsMapper;

  @Override
  public List<TermsDTO> getList() {
    List<Terms> list = termsRepository.findAll();
    return list.stream().map(termsMapper::toDTO).toList();
  }
}
