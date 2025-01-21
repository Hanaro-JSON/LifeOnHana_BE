package com.example.lifeonhana;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LifeonhanaApplication {

	public static void main(String[] args) {
		SpringApplication.run(LifeonhanaApplication.class, args);
	}

}
