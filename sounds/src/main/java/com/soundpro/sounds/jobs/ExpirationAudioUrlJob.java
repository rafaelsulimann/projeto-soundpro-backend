package com.soundpro.sounds.jobs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.soundpro.sounds.services.SoundService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ExpirationAudioUrlJob {

    @Autowired
    private SoundService soundService;

    @Scheduled(cron = "0 */30 * * * *")
    public void executar() {
        log.info("Iniciando ExpirationAudioUrlJob ...");
        this.soundService.updateExpirationAudioUrl();
        log.info("Finalizando ExpirationAudioUrlJob!");
    }

}
