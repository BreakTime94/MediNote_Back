package com.medinote.medinote_back_kc.security.service;

import com.medinote.medinote_back_kc.security.dto.AuthMemberDTO;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

  private final AuthMemberDTO member;

  public CustomUserDetails (AuthMemberDTO member) {
    this.member = member;
  }

  public AuthMemberDTO getMember() {
    return member;
  }

  @Override
  public String getUsername() {
    return member.getEmail();
  }

  @Override
  public String getPassword() {
    return member.getPassword();
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + member.getRole()));
  }

  @Override
  public boolean isEnabled() {
    return member.getStatus().equals("ACTIVE");
  }

  @Override
  public boolean isAccountNonLocked() {
    return !member.getStatus().equals("DISABLED");
  }
}
