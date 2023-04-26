package com.soundpro.sounds.jobs;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.soundpro.sounds.configs.FirebaseStorageConfig;
import com.soundpro.sounds.models.Sound;
import com.soundpro.sounds.repositories.SoundRepository;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ExpirationAudioUrlJob {

    @Value("${soundpro.firebase.storage.bucket}")
    private String bucketName;

    @Autowired
    private SoundRepository soundRepository;

    @Autowired
    private FirebaseStorageConfig firebaseStorageConfig;
    
    @Scheduled(cron = "0 0 0 * * *")
    public void executar(){
        log.info("Iniciando ExpirationAudioUrlJob ...");
        List<Sound> list = soundRepository.findAll();
        if(list.isEmpty()){
            log.info("Não há nenhum audio cadastrado no banco de dados");
            return;
        }
        log.info("Verificando expiração de url de {} audios", list.size());
        int count = 0;
        list.forEach(sound -> {
            LocalDateTime expirationDate = sound.getExpirationDateAudioTokenUrl();
            LocalDateTime validationDate = LocalDateTime.now(ZoneId.of("UTC")).plusDays(1);
            if (expirationDate.isBefore(validationDate)) {
                log.info("Audio {} - {}", (count + 1), sound.getName());
                log.info("O Url de referencia do audio {} vai expirar em menos de um dia, criando nova url", sound.getName());
                BlobId blobId = BlobId.of(bucketName, sound.getName());
                try {
                    Blob blob = firebaseStorageConfig.storage().get(blobId);
                    URL signedUrl = blob.signUrl(7, TimeUnit.DAYS);
                    log.info("Nova url de referencia criada para o audio {}, url: {}", sound.getName(), signedUrl.toString());
                    LocalDateTime creationDateAudioUrlToken = LocalDateTime.now(ZoneId.of("UTC"));
                    LocalDateTime expirationDateAudioUrlToken = creationDateAudioUrlToken.plusDays(7);
                    sound.setAudioUrl(signedUrl.toString());
                    sound.setCreationDateAudioTokenUrl(creationDateAudioUrlToken);
                    sound.setExpirationDateAudioTokenUrl(expirationDateAudioUrlToken);
                    soundRepository.save(sound);
                    log.info("Audio {} atualizado com sucesso!", sound.getName());
                } catch (IOException e) {
                    log.error("Erro ao criar nova url de referencia para o audio {}", sound.getName());
                    e.printStackTrace();
                }
            }
        });
        log.info("Finalizando ExpirationAudioUrlJob!");
    }
}
