package com.medinote.medinote_back_kc.member.service.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {
  private final JavaMailSender javaMailSender;

  public void sendEmail(String to, String subject, String text) {
    SimpleMailMessage mail = new SimpleMailMessage();
    mail.setTo(to);
    mail.setSubject(subject);
    mail.setText(text);
    javaMailSender.send(mail);
  }
}
