package com.soundpro.sounds;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.soundpro.sounds.repositories.SoundRepository;

@SpringBootApplication
@EnableEurekaClient
@EnableScheduling
public class SoundsApplication implements CommandLineRunner{

	@Autowired
	private SoundRepository soundRepository;

	public static void main(String[] args) {
		SpringApplication.run(SoundsApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		soundRepository.deleteAll();
	}

}
