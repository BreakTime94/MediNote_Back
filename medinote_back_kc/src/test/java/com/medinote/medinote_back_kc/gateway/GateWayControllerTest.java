package com.medinote.medinote_back_kc.gateway;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@Log4j2
public class GateWayControllerTest {

  @Autowired
  private RestTemplate restTemplate;

  @Test
  public void patchTest() {
   log.info("RestTemplate RequestFactory = {}", restTemplate.getRequestFactory().getClass());
  }
}
