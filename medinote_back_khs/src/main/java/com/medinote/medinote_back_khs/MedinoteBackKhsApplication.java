package com.medinote.medinote_back_khs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class MedinoteBackKhsApplication {

	public static void main(String[] args) {
		SpringApplication.run(MedinoteBackKhsApplication.class, args);
	}

}
