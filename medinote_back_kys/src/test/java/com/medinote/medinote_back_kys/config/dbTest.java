package com.medinote.medinote_back_kys.config;

import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.core.util.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;

@SpringBootTest
@Log4j2
public class dbTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void testConnection() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            log.info("DB 연결 성공: {}", conn.getMetaData().getURL());
            Assert.isNonEmpty(conn);
//            assertThat(conn).isNotNull(); // 연결 객체가 null이 아님을 검증
        }
    }
}
