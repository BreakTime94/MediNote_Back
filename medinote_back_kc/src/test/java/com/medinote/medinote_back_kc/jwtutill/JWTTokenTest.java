package com.medinote.medinote_back_kc.jwtutill;

import com.medinote.medinote_back_kc.security.util.JWTUtil;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Log4j2
public class JWTTokenTest {

  private JWTUtil jwtUtil;

  @Test
  public void testExist() {
    log.info("{}",jwtUtil);
  }

  @Test
  public void testGetToken() {

  }
}
