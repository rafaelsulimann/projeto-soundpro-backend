package com.soundpro.sounds.services;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.soundpro.sounds.configs.FirebaseStorageConfig;
import com.soundpro.sounds.dtos.SoundDTO;
import com.soundpro.sounds.enums.SoundType;
import com.soundpro.sounds.models.Sound;
import com.soundpro.sounds.repositories.SoundRepository;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class SoundService {

    @Autowired
    private FirebaseStorageConfig firebaseStorageConfig;

    @Value("${soundpro.firebase.storage.bucket}")
    private String bucketName;

    @Autowired
    private SoundRepository soundRepository;

    public Page<SoundDTO> findAll(Pageable pageable) {
        return soundRepository.findAll(pageable).map(sound -> new SoundDTO(sound));
    }

    public SoundDTO insert(String name, MultipartFile audio) throws IOException {
        byte[] bytes = audio.getBytes();
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, name)
                .setContentType("audio/mpeg")
                .build();
        Blob blob = firebaseStorageConfig.storage().create(blobInfo, bytes);

        URL signedUrl = blob.signUrl(7, TimeUnit.DAYS);
        LocalDateTime creationDate = LocalDateTime.now(ZoneId.of("UTC"));
        LocalDateTime lastUpdateDate = LocalDateTime.now(ZoneId.of("UTC"));

        LocalDateTime creationDateAudioUrlToken = LocalDateTime.now(ZoneId.of("UTC"));
        LocalDateTime expirationDateAudioUrlToken = creationDateAudioUrlToken.plusDays(7);

        SoundDTO dto = new SoundDTO(null, name, signedUrl.toString(),creationDate, lastUpdateDate, creationDateAudioUrlToken, expirationDateAudioUrlToken, name.toLowerCase().contains("mp3") ? SoundType.MP3 : SoundType.WAV);

        Sound entity = dto.convertToEntity(dto);
        entity = soundRepository.insert(entity);
        return new SoundDTO(entity);
    }

}
