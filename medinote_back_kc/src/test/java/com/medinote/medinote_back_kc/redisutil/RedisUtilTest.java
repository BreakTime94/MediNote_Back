package com.medinote.medinote_back_kc.redisutil;

import com.medinote.medinote_back_kc.member.domain.entity.Role;
import com.medinote.medinote_back_kc.security.util.JWTUtil;
import com.medinote.medinote_back_kc.security.util.RedisUtil;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

@SpringBootTest
@Log4j2
public class RedisUtilTest {
  @Autowired
  private RedisUtil redisUtil;
  @Autowired
  private JWTUtil jwtUtil;

  @Test
  public void testExist() {
    Assertions.assertNotNull(redisUtil);
  }

  @Test
  public void testSetAndGet() {
    String key = "testKey";
    String value = "HelloRedis";

    redisUtil.set(key, value, 5000);

    String result = redisUtil.get(key);

    log.info("testKey로 찾은 value 값은 ! : {}", result);

    Assertions.assertEquals(value, result);
  }

  @Test
  void testHasKey() {
    String key = "existKey";
    redisUtil.set(key, "값", 3000);
    log.info(redisUtil.get(key));
    Assertions.assertTrue(redisUtil.hasKey(key));
  }

  @Test
  void testDelete() {
    String key = "deleteKey";
    redisUtil.set(key, "사라질 녀석", 3000);
    log.info(redisUtil.get(key));
    redisUtil.delete(key);
    log.info(redisUtil.get(key));
    Assertions.assertFalse(redisUtil.hasKey(key));
    Assertions.assertNull(redisUtil.get(key));
  }

  @Test
  void testTTL() {//refreshToken과 실제 redis에 저장된 토큰의 유효기간 차이 확인
    String testRefreshToken = jwtUtil.createRefreshToken(1L, Role.USER);


    // 2. 토큰 만료 시각
    Date exp = jwtUtil.getExpirationDate(testRefreshToken);
    log.info("토큰 만료 시각 = " + exp);

    // 3. TTL 계산 후 Redis에 저장
    long ttl = exp.getTime() - System.currentTimeMillis();

    redisUtil.set(jwtUtil.getUserId(testRefreshToken).toString(), testRefreshToken, ttl);

    // 4. Redis TTL 확인 (초 단위)
    long ttlInRedis = redisUtil.getTtl("1");
    log.info("Redis TTL = " + ttlInRedis + "초");

    // 5. 검증 (만료 시각과 TTL이 일치하는지)
    long expectedExpireTime = System.currentTimeMillis() + ttlInRedis * 1000;
    log.info("예상 만료 시각 = " + new Date(expectedExpireTime));

    // assert (±2초 차이는 허용)
    Assertions.assertTrue(Math.abs(exp.getTime() - expectedExpireTime) < 2000);
  }

}
