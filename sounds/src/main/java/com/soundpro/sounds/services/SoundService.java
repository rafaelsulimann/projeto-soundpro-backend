package com.soundpro.sounds.services;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.soundpro.sounds.dtos.FirebaseAudioDTO;
import com.soundpro.sounds.dtos.SoundDTO;
import com.soundpro.sounds.enums.SoundType;
import com.soundpro.sounds.models.Sound;
import com.soundpro.sounds.repositories.SoundRepository;
import com.soundpro.sounds.services.exceptions.ConvertMultipartFileToBytesException;
import com.soundpro.sounds.services.exceptions.ResourceNotFoundException;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class SoundService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private SoundRepository soundRepository;

    @Autowired
    private FirebaseStorageService firebaseStorageService;
    

    public Page<SoundDTO> findAll(Pageable pageable) {
        return soundRepository.findAll(pageable).map(sound -> new SoundDTO(sound));
    }

    public SoundDTO insert(MultipartFile audio) {
        try {
            String nameWithoutExtension = audio.getOriginalFilename().replaceAll("(?i)\\.(mp3|wav)$", "");
            log.info("POST recebido, name: {}, audio: {}", nameWithoutExtension, audio);
            FirebaseAudioDTO firebaseAudio = firebaseStorageService.insertAudio(nameWithoutExtension, audio.getBytes());
            LocalDateTime creationDate = LocalDateTime.now(ZoneId.of("UTC"));
            LocalDateTime lastUpdateDate = LocalDateTime.now(ZoneId.of("UTC"));
    
            SoundDTO dto = new SoundDTO(null, nameWithoutExtension, firebaseAudio.getSignedUrl(), creationDate, lastUpdateDate,
                    audio.getOriginalFilename().toLowerCase().contains("mp3") ? SoundType.MP3 : SoundType.WAV, false);
    
            Sound entity = dto.convertToEntity(dto, firebaseAudio);
            entity = soundRepository.insert(entity);
            log.info("Sound salvo no banco de dados com sucesso! Sound: {}", entity.toString());
            return new SoundDTO(entity);
        } catch (IOException e) {
            log.error("Error converter o arquivo para bytes. Error: {}", e.toString());
            throw new ConvertMultipartFileToBytesException("Error converter o arquivo para bytes");
        }
    }

    public void delete(String soundId) {
        try {
            Sound entity = this.soundRepository.findById(soundId).orElseThrow(() -> new ResourceNotFoundException("Sound não encontrado"));
            log.info("Deletando sound {} do banco de dados", entity.getId());
            this.soundRepository.deleteById(soundId);
            log.info("Sound {} deletado com sucesso do banco de dados", entity.getId());
            this.firebaseStorageService.deleteSound(entity.getName());
            this.kafkaTemplate.send("delete-sound-firebase", entity.getName());
        } catch (Exception e) {
            log.error("Erro ao deletar sound do banco de dados. Error: {}", e.toString());
        }
    }

}
