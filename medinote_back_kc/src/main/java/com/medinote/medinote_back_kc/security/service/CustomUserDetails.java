package com.medinote.medinote_back_kc.security.service;

import com.medinote.medinote_back_kc.member.domain.entity.member.Member;
import com.medinote.medinote_back_kc.member.domain.entity.member.Role;
import com.medinote.medinote_back_kc.member.domain.entity.member.Status;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@Log4j2
public class CustomUserDetails implements UserDetails { // AuthDTO 같은 역할을 하며, Security Context에 등록될 친구

  private final Long id;
  private final String email;
  private final Role role;
  private final Status status;


  public CustomUserDetails(Member member) {
    this.id = member.getId();
    this.email = member.getEmail();
    this.role = member.getRole();
    this.status = member.getStatus();
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    log.info("계정이 잠겨있습니다. 관리자에게 문의하여 주십시오.");
    return status != Status.DISABLED;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + role));
  }

  @Override
  public String getPassword() {
    return "";
  }

  @Override
  public boolean isEnabled() {
    log.info("계정이 삭제되었습니다. 신규 회원가입을 하시거나 다른 계정을 활용하여 주시기 바랍니다.");
    return status != Status.DELETED;
  }

  @Override
  public String getUsername() {
    return this.email;
  }
}
