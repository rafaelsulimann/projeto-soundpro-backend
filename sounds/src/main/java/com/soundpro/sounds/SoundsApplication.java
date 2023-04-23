package com.soundpro.sounds;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

import com.soundpro.sounds.repositories.SoundTesteRepository;

@SpringBootApplication
@EnableEurekaClient
public class SoundsApplication implements CommandLineRunner{

	@Autowired
	private SoundTesteRepository soundTesteRepository;

	public static void main(String[] args) {
		SpringApplication.run(SoundsApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		soundTesteRepository.deleteAll();
	}

}
