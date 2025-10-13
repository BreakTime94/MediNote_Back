package com.medinote.medinote_back_kc.member.service.Terms;

import com.medinote.medinote_back_kc.member.domain.dto.terms.TermsDTO;
import java.util.List;

public interface TermsService {
  List<TermsDTO> getList();
}
