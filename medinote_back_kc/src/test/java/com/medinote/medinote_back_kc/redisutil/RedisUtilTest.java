package com.medinote.medinote_back_kc.redisutil;

import com.medinote.medinote_back_kc.security.util.RedisUtil;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Log4j2
public class RedisUtilTest {
  @Autowired
  private RedisUtil redisUtil;

  @Test
  public void testExist() {
    Assertions.assertNotNull(redisUtil);
  }

  @Test
  public void testSetAndGet() {
    String key = "testKey";
    String value = "HelloRedis";

    redisUtil.set(key, value, 15000);

    String result = redisUtil.get(key);

    log.info("testKey로 찾은 value 값은 ! : {}", result);

    Assertions.assertEquals(value, result);
  }
}
