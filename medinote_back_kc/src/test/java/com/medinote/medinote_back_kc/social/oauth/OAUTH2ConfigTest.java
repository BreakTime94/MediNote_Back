package com.medinote.medinote_back_kc.social.oauth;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

@SpringBootTest
@Log4j2
public class OAUTH2ConfigTest {

  @Autowired
  private ClientRegistrationRepository clientRegistrationRepository;

  @Test
  public void checkGoogleClientRegistration() {
    ClientRegistration google = ((InMemoryClientRegistrationRepository) clientRegistrationRepository)
            .findByRegistrationId("google");
    .

    Assertions.assertNotNull(google, "Google OAuth2 client registration is not loaded!");

    log.info("Loaded Client :{}", google);
    log.info("Google OAuth2 client registration id :{}", google.getClientId());
    log.info("redirectUri : {}", google.getRedirectUri());

  }
}
