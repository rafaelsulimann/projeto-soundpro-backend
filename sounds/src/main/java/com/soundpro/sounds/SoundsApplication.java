package com.soundpro.sounds;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
//@EnableEurekaClient
@EnableScheduling
public class SoundsApplication {

	public static void main(String[] args) {
		SpringApplication.run(SoundsApplication.class, args);
	}

}
