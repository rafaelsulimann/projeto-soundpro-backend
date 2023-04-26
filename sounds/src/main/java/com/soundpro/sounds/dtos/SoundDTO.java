package com.soundpro.sounds.dtos;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.soundpro.sounds.enums.SoundType;
import com.soundpro.sounds.models.Sound;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SoundDTO implements Serializable{

    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String audioUrl;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime creationDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime lastUpdatedDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime creationDateAudioTokenUrl;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime expirationDateAudioTokenUrl;

    private SoundType soundType;

    public SoundDTO(Sound entity){
        id = entity.getId();
        name = entity.getName();
        audioUrl = entity.getAudioUrl();
        creationDate = entity.getCreationDate();
        lastUpdatedDate = entity.getLastUpdatedDate();
        creationDateAudioTokenUrl = entity.getCreationDateAudioTokenUrl();
        expirationDateAudioTokenUrl = entity.getExpirationDateAudioTokenUrl();
        soundType = entity.getSoundType();
    }

    public Sound convertToEntity(SoundDTO dto){
        return new Sound(null, dto.getName(), dto.getAudioUrl(), dto.getCreationDate(), dto.getLastUpdatedDate(),dto.getCreationDateAudioTokenUrl(), dto.getExpirationDateAudioTokenUrl(), dto.getSoundType());
    }

    
}
