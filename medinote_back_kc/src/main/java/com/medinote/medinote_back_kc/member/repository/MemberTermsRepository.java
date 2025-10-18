package com.medinote.medinote_back_kc.member.repository;

import com.medinote.medinote_back_kc.member.domain.entity.terms.MemberTerms;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberTermsRepository extends JpaRepository<MemberTerms, Long> {
}
