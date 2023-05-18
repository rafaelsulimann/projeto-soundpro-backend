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
import com.soundpro.sounds.dtos.UpdateSoundFirebaseDTO;
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
            BlobInfo blobInfo = BlobInfo.newBuilder(this.bucketName, nameWithoutExtension).setContentType("audio/mpeg")
                    .build();
            log.info("Salvando BLOBINFO no FirebaseStorage. BLOBINFO {}", blobInfo);
            Blob blob = this.firebaseStorageConfig.storage().create(blobInfo, bytes);
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
            e.printStackTrace();
            throw new FirebaseStorageException("Error ao validar audio e salvar no Firebase Storage");
        }
    }

    public FirebaseAudioDTO createNewSignedUrl(String audioName) {
        try {
            BlobId blobId = BlobId.of(this.bucketName, audioName);
            Blob blob = this.firebaseStorageConfig.storage().get(blobId);
            URL signedUrl = blob.signUrl(7, TimeUnit.DAYS);
            log.info("Nova url de referencia criada para o audio {}, url: {}", audioName, signedUrl.toString());
            LocalDateTime creationDateAudioUrlToken = LocalDateTime.now(ZoneId.of("UTC"));
            LocalDateTime expirationDateAudioUrlToken = creationDateAudioUrlToken.plusDays(7);
            return new FirebaseAudioDTO(signedUrl.toString(), creationDateAudioUrlToken, expirationDateAudioUrlToken);
        } catch (IOException e) {
            log.error("Erro ao criar nova url de referencia para o audio {}: Error: {}", audioName, e.toString());
            e.printStackTrace();
            throw new FirebaseStorageException("Error ao validar audio e salvar no Firebase Storage");
        }
    }

    public void deleteSound(String soundName){
        try {
            log.info("Deletando sound {} do firebase storage", soundName);
            this.firebaseStorageConfig.storage().delete(BlobId.of(this.bucketName, soundName));
        } catch (IOException e) {
            log.error("Erro ao deletar sound {} do firebase storage: Error: {}", soundName, e.toString());
            e.printStackTrace();
            throw new FirebaseStorageException("Error ao deletar sound do Firebase Storage");
        }
    }
    
    public FirebaseAudioDTO updateSound(UpdateSoundFirebaseDTO updateSoundFirebaseDTO) {
        try {
            BlobId blobId = BlobId.of(this.bucketName, updateSoundFirebaseDTO.getOriginalSoundName());
            Blob blob = this.firebaseStorageConfig.storage().get(blobId);
            log.info("Atualizando o nome do sound {} para {}", updateSoundFirebaseDTO.getOriginalSoundName(), updateSoundFirebaseDTO.getNewSoundName());
            blob.copyTo(BlobId.of(this.bucketName, updateSoundFirebaseDTO.getNewSoundName()));
            log.info("Deletando sound com nome antigo {} do firebase storage", updateSoundFirebaseDTO.getOriginalSoundName());
            this.firebaseStorageConfig.storage().delete(blobId);
            return this.createNewSignedUrl(updateSoundFirebaseDTO.getNewSoundName());
        } catch (IOException e) {
            log.error("Erro ao atualizar o nome do sound {} para {} do firebase storage: Error: {}", updateSoundFirebaseDTO.getOriginalSoundName(), updateSoundFirebaseDTO.getNewSoundName(), e.toString());
            e.printStackTrace();
            throw new FirebaseStorageException("Error ao atualizar sound do Firebase Storage");
        }

    }

}
