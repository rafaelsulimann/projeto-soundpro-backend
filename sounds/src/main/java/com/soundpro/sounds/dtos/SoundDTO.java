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

    private SoundType soundType;
    private Boolean liked;

    public SoundDTO(Sound entity){
        id = entity.getId();
        name = entity.getName();
        audioUrl = entity.getAudioUrl();
        creationDate = entity.getCreationDate();
        lastUpdatedDate = entity.getLastUpdatedDate();
        soundType = entity.getSoundType();
        liked = entity.getLiked();
    }
    
}
