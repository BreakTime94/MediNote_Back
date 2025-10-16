package com.medinote.medinote_back_kc.mail;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;

@SpringBootTest
@Log4j2
public class MailServiceTest {
  @Autowired
  private JavaMailSender javaMailSender;
  @Test
  public void testExist() {
    Assertions.assertNotNull(javaMailSender);
    log.info("javaMailSender:{}", javaMailSender);
  }
}
