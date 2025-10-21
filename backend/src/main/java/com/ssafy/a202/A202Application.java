package com.ssafy.a202;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class A202Application {

	public static void main(String[] args) {
		SpringApplication.run(A202Application.class, args);
	}

}
