package com.medinote.medinote_back_kc.member.repository;

import com.medinote.medinote_back_kc.member.domain.entity.terms.Terms;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermsRepository extends JpaRepository<Terms, Long> {
}
