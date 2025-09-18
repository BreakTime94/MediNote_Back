package com.medinote.medinote_back_kys;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class MedinoteBackKysApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedinoteBackKysApplication.class, args);
    }

}
