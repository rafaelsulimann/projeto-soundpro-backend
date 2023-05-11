package com.soundpro.sounds.services;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.soundpro.sounds.configs.FirebaseStorageConfig;
import com.soundpro.sounds.dtos.FirebaseAudioDTO;
import com.soundpro.sounds.services.exceptions.FirebaseStorageException;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class FirebaseStorageService {

    @Autowired
    private FirebaseStorageConfig firebaseStorageConfig;

    @Value("${soundpro.firebase.storage.bucket}")
    private String bucketName;

    public FirebaseAudioDTO insertAudio(String nameWithoutExtension, byte[] bytes) {
        try {
            log.info("Criando BLOBINFO...");
            BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, nameWithoutExtension).setContentType("audio/mpeg")
                    .build();
            log.info("Salvando BLOBINFO no FirebaseStorage. BLOBINFO {}", blobInfo);
            Blob blob = firebaseStorageConfig.storage().create(blobInfo, bytes);
            log.info("BLOB salvo no Fibase Storage. BLOB: {}", blob);

            URL signedUrl = blob.signUrl(7, TimeUnit.DAYS);

            LocalDateTime creationDateAudioUrlToken = LocalDateTime.now(ZoneId.of("UTC"));
            LocalDateTime expirationDateAudioUrlToken = creationDateAudioUrlToken.plusDays(7);
            log.info("Criado url de referencia para o audio {}. URL: {}, Expiração: {}", nameWithoutExtension,
                    signedUrl,
                    expirationDateAudioUrlToken.toString());
            return new FirebaseAudioDTO(signedUrl.toString(), creationDateAudioUrlToken, expirationDateAudioUrlToken);
        } catch (IOException e) {
            log.error("Error ao validar audio e salvar no Firebase Storage. Error: {}", e.toString());
            throw new FirebaseStorageException("Error ao validar audio e salvar no Firebase Storage");
        }
    }

    public FirebaseAudioDTO updateSoundSignedUrl(String audioName) {
        try {
            BlobId blobId = BlobId.of(bucketName, audioName);
            Blob blob = firebaseStorageConfig.storage().get(blobId);
            URL signedUrl = blob.signUrl(7, TimeUnit.DAYS);
            log.info("Nova url de referencia criada para o audio {}, url: {}", audioName, signedUrl.toString());
            LocalDateTime creationDateAudioUrlToken = LocalDateTime.now(ZoneId.of("UTC"));
            LocalDateTime expirationDateAudioUrlToken = creationDateAudioUrlToken.plusDays(7);
            return new FirebaseAudioDTO(signedUrl.toString(), creationDateAudioUrlToken, expirationDateAudioUrlToken);
        } catch (IOException e) {
            log.error("Erro ao criar nova url de referencia para o audio {}: Error: {}", audioName, e.toString());
            throw new FirebaseStorageException("Error ao validar audio e salvar no Firebase Storage");
        }
    }

    public void deleteSound(String soundName){
        try {
            log.info("Deletando sound {} do firebase storage", soundName);
            firebaseStorageConfig.storage().delete(BlobId.of(bucketName, soundName));
        } catch (IOException e) {
            log.error("Erro ao deletar sound {} do firebase storage: Error: {}", soundName, e.toString());
        }
    }

}
