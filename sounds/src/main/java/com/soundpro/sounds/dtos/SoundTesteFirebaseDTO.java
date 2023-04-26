package com.soundpro.sounds.dtos;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.soundpro.sounds.models.SoundTesteFirebase;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SoundTesteFirebaseDTO implements Serializable{
    
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String audioUrl;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime creationDateAudioTokenUrl;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime expirationDateAudioTokenUrl;

    public SoundTesteFirebaseDTO(SoundTesteFirebase entity){
        id = entity.getId();
        name = entity.getName();
        audioUrl = entity.getAudioUrl();
        creationDateAudioTokenUrl = entity.getCreationDateAudioTokenUrl();
        expirationDateAudioTokenUrl = entity.getExpirationDateAudioTokenUrl();
    }

    public SoundTesteFirebase convertToEntity(SoundTesteFirebaseDTO dto){
        return new SoundTesteFirebase(null, dto.getName(), dto.getAudioUrl(), dto.getCreationDateAudioTokenUrl(), dto.getExpirationDateAudioTokenUrl());
    }
}
