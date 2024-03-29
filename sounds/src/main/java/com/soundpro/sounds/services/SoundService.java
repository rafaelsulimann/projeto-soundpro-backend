package com.soundpro.sounds.services;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.soundpro.sounds.constants.KafkaTopic;
import com.soundpro.sounds.dtos.DownloadSoundDTO;
import com.soundpro.sounds.dtos.FirebaseAudioDTO;
import com.soundpro.sounds.dtos.LoadingFileDTO;
import com.soundpro.sounds.dtos.SoundDTO;
import com.soundpro.sounds.dtos.SoundUpdateRequestDTO;
import com.soundpro.sounds.dtos.UpdateSoundFirebaseDTO;
import com.soundpro.sounds.dtos.YoutubeConverterDTO;
import com.soundpro.sounds.enums.SoundType;
import com.soundpro.sounds.models.Sound;
import com.soundpro.sounds.repositories.SoundRepository;
import com.soundpro.sounds.services.exceptions.ConvertMultipartFileToBytesException;
import com.soundpro.sounds.services.exceptions.ResourceNotFoundException;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class SoundService extends AbstractService{

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private SoundRepository soundRepository;

    @Autowired
    private FirebaseStorageService firebaseStorageService;

    @Autowired
    private YoutubeConverterService youtubeConverterService;
    

    public Page<SoundDTO> findAll(Pageable pageable) {
        return this.soundRepository.findAll(pageable).map(sound -> new SoundDTO(sound));
    }

    public Page<SoundDTO> findSoundByName(String name, Pageable pageable){
        return this.soundRepository.findSoundByName(name, pageable).map(sound -> new SoundDTO(sound));
    }

    public SoundDTO findById(String soundId){
        return new SoundDTO(this.findSoundById(soundId));
    }

    public SoundDTO insert(MultipartFile audio) {
        try {
            String nameWithoutExtension = audio.getOriginalFilename().replaceAll("(?i)\\.(mp3|wav)$", "");
            String nameWithFirstLetterUppercase = nameWithoutExtension.substring(0, 1).toUpperCase() + nameWithoutExtension.substring(1);
            log.info("POST recebido, name: {}, audio: {}", nameWithFirstLetterUppercase, audio);
            FirebaseAudioDTO firebaseAudio = this.firebaseStorageService.insertAudio(nameWithFirstLetterUppercase, audio.getBytes());
            LocalDateTime creationDate = LocalDateTime.now(ZoneId.of("UTC"));
            LocalDateTime lastUpdateDate = LocalDateTime.now(ZoneId.of("UTC"));
    
            SoundDTO dto = new SoundDTO(null, nameWithFirstLetterUppercase, firebaseAudio.getSignedUrl(), creationDate, lastUpdateDate,
                    audio.getOriginalFilename().toLowerCase().contains("mp3") ? SoundType.MP3 : SoundType.WAV, false);
    
            Sound entity = this.convertToEntity(dto, firebaseAudio);
            entity = this.soundRepository.insert(entity);
            log.info("Sound salvo no banco de dados com sucesso! Sound: {}", entity.toString());
            return new SoundDTO(entity);
        } catch (IOException e) {
            log.error("Error converter o arquivo para bytes. Error: {}", e.toString());
            throw new ConvertMultipartFileToBytesException("Error converter o arquivo para bytes");
        }
    }

    public void delete(String soundId) {
        try {
            Sound entity = this.findSoundById(soundId);
            log.info("Deletando sound {} do banco de dados", entity.getId());
            this.soundRepository.deleteById(soundId);
            log.info("Sound {} deletado com sucesso do banco de dados", entity.getId());
            this.kafkaTemplate.send(KafkaTopic.DELETE_SOUND_FIREBASE, entity.getName()); //Enviando mensagem para o DeleteSoundListener deletar o sound específico do firebase storage
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Erro ao deletar sound do banco de dados. Error: {}", e.toString());
        }
    }
    
    public SoundDTO update(String soundId, SoundUpdateRequestDTO soundUpdateRequestDTO){
        try {
            Sound entity = this.findSoundById(soundId);
            String newSoundName = soundUpdateRequestDTO.getSoundName().substring(0, 1).toUpperCase() + soundUpdateRequestDTO.getSoundName().substring(1);
            String originalSoundName = entity.getName();
            if(originalSoundName != newSoundName){
                FirebaseAudioDTO newFirebaseAudio = firebaseStorageService.updateSound(new UpdateSoundFirebaseDTO(originalSoundName, newSoundName));
                log.info("Sound {} sendo atualizado para o nome {}", entity.getName(), newSoundName);
                soundUpdateRequestDTO.setSoundName(newSoundName);
                this.updateEntityWithNewFirebaseAudio(entity, soundUpdateRequestDTO, newFirebaseAudio);
                log.info("Salvando Sound com o novo nome do banco de dados...");
                this.soundRepository.save(entity);
                log.info("Sound {} salvo com sucesso no banco de dados com o novo nome!");
                return new SoundDTO(entity);
            }
            log.info("Atualizando o like do sound {} para {}", entity.getName(), soundUpdateRequestDTO.getLiked());
            entity.setLiked(soundUpdateRequestDTO.getLiked());
            log.info("Salvando Sound com o status de like no banco de dados...");
            this.soundRepository.save(entity);
            log.info("Sound {} salvo com sucesso no banco de dados com novo status de like!");
            return new SoundDTO(entity);
        } catch (Exception e) {
            log.error("Erro ao atualizar sound no banco de dados. Error: {}", e.toString());
            e.printStackTrace();
            return null;
        }
    }

    public DownloadSoundDTO findSoundBytesByName(String soundId){
        Sound entity = this.findSoundById(soundId);
        byte[] audioContent = this.firebaseStorageService.getSoundBytesByName(entity.getName());
        return new DownloadSoundDTO(entity.getName(), audioContent);
    }
    
    public SoundDTO insertWithYoutubeUrl(YoutubeConverterDTO youtubeConverterDTO) {
        LoadingFileDTO loadingFileDTO = new LoadingFileDTO("Carregando", 0, 0);
        this.webSocketService.sendMessage("/topic/progress/" + youtubeConverterDTO.getRequestId(), super.jsonMapper().toJson(loadingFileDTO));
        MultipartFile mp3File = this.youtubeConverterService.convertYoutubeVideoUrlToMp3MultipartFile(youtubeConverterDTO, loadingFileDTO);
        loadingFileDTO.setProgressPercentage(70);
        this.webSocketService.sendMessage("/topic/progress/" + youtubeConverterDTO.getRequestId(), super.jsonMapper().toJson(loadingFileDTO));
        SoundDTO entity = this.insert(mp3File);
        loadingFileDTO.setProgressPercentage(100);
        this.webSocketService.sendMessage("/topic/progress/" + youtubeConverterDTO.getRequestId(), super.jsonMapper().toJson(loadingFileDTO));
        boolean deletado = this.deleteMp3FileFromAplicationDirectory(mp3File);
        if (deletado) {
            log.info("Arquivo mp3 {} deletado com sucesso.", mp3File.getOriginalFilename());
        } else {
            log.info("Erro ao deletar arquivo mp3 {}", mp3File.getOriginalFilename());
        }
        return entity;
    }

    public void updateExpirationAudioUrl(){
        List<Sound> list = this.soundRepository.findAll();

        if (list.isEmpty()) {
            log.info("Nao ha nenhum audio cadastrado no banco de dados");
            return;
        }

        log.info("Verificando expiração de url de {} audios", list.size());

        list.forEach(sound -> {
            LocalDateTime expirationDate = sound.getExpirationDateAudioTokenUrl();
            LocalDateTime validationDate = LocalDateTime.now(ZoneId.of("UTC")).plusMinutes(40);

            if (expirationDate.isBefore(validationDate)) {
                log.info("O Url de referencia do audio {} vai expirar em menos de um dia, criando nova url",
                        sound.getName());
                FirebaseAudioDTO newSignedUrl = this.firebaseStorageService.createNewSignedUrl(sound.getName());
                this.updateNewSignedUrlInEntity(sound, newSignedUrl);
                this.soundRepository.save(sound);
                log.info("Audio {} atualizado com sucesso!", sound.getName());
            }
        });
    }

    private Sound findSoundById(String soundId){
        return this.soundRepository.findById(soundId).orElseThrow(() -> new ResourceNotFoundException("Sound não encontrado"));
    }

    private Sound convertToEntity(SoundDTO dto, FirebaseAudioDTO firebaseAudioDTO){
        return new Sound(null, dto.getName(), dto.getAudioUrl(), dto.getCreationDate(), dto.getLastUpdatedDate(), firebaseAudioDTO.getCreationDateAudioUrlToken(), firebaseAudioDTO.getExpirationDateAudioUrlToken(), dto.getSoundType(), dto.getLiked());
    }

    private void updateEntityWithNewFirebaseAudio(Sound entity, SoundUpdateRequestDTO soundUpdateRequestDTO, FirebaseAudioDTO firebaseAudioDTO){
        entity.setName(soundUpdateRequestDTO.getSoundName());
        entity.setCreationDateAudioTokenUrl(firebaseAudioDTO.getCreationDateAudioUrlToken());
        entity.setExpirationDateAudioTokenUrl(firebaseAudioDTO.getExpirationDateAudioUrlToken());
        entity.setAudioUrl(firebaseAudioDTO.getSignedUrl());
        entity.setLastUpdatedDate(LocalDateTime.now(ZoneId.of("UTC")));
    }

    private boolean deleteMp3FileFromAplicationDirectory(MultipartFile mp3File){
        String caminhoMp3 = System.getProperty("user.home") + "/youtube-videos/" + mp3File.getOriginalFilename();
        File arquivoMP3 = new File(caminhoMp3);
        if (arquivoMP3.exists()) {
            return arquivoMP3.delete();
        }
        return false;
    }

     private void updateNewSignedUrlInEntity(Sound sound, FirebaseAudioDTO newSignedUrl){
        sound.setAudioUrl(newSignedUrl.getSignedUrl());
        sound.setCreationDateAudioTokenUrl(newSignedUrl.getCreationDateAudioUrlToken());
        sound.setExpirationDateAudioTokenUrl(newSignedUrl.getExpirationDateAudioUrlToken());
    }

}
