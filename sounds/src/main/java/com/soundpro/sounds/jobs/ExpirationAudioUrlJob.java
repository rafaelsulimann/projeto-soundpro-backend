package com.soundpro.sounds.jobs;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.soundpro.sounds.dtos.FirebaseAudioDTO;
import com.soundpro.sounds.models.Sound;
import com.soundpro.sounds.repositories.SoundRepository;
import com.soundpro.sounds.services.FirebaseStorageService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ExpirationAudioUrlJob {

    @Autowired
    private SoundRepository soundRepository;

    @Autowired
    private FirebaseStorageService firebaseStorageService;

    @Scheduled(cron = "0 */30 * * * *")
    public void executar() {
        log.info("Iniciando ExpirationAudioUrlJob ...");

        List<Sound> list = soundRepository.findAll();

        if (list.isEmpty()) {
            log.info("Não há nenhum audio cadastrado no banco de dados");
            return;
        }

        log.info("Verificando expiração de url de {} audios", list.size());

        list.forEach(sound -> {
            LocalDateTime expirationDate = sound.getExpirationDateAudioTokenUrl();
            LocalDateTime validationDate = LocalDateTime.now(ZoneId.of("UTC")).plusMinutes(40);

            if (expirationDate.isBefore(validationDate)) {
                log.info("O Url de referencia do audio {} vai expirar em menos de um dia, criando nova url",
                        sound.getName());
                FirebaseAudioDTO newSignedUrl = this.firebaseStorageService.updateSoundSignedUrl(sound.getName());
                this.updateSignedUrlInEntity(sound, newSignedUrl);
                this.soundRepository.save(sound);
                log.info("Audio {} atualizado com sucesso!", sound.getName());
            }
        });
        
        log.info("Finalizando ExpirationAudioUrlJob!");
    }

    private void updateSignedUrlInEntity(Sound sound, FirebaseAudioDTO newSignedUrl){
        sound.setAudioUrl(newSignedUrl.getSignedUrl());
        sound.setCreationDateAudioTokenUrl(newSignedUrl.getCreationDateAudioUrlToken());
        sound.setExpirationDateAudioTokenUrl(newSignedUrl.getExpirationDateAudioUrlToken());
    }
}
