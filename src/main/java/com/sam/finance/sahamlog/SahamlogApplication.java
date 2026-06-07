package com.sam.finance.sahamlog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SahamlogApplication {

	public static void main(String[] args) {
		SpringApplication.run(SahamlogApplication.class, args);
	}

}
