package com.soundpro.sounds.models;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.soundpro.sounds.enums.SoundType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Document(collection = "sounds")
public class Sound implements Serializable{
    
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    private String name;
    private String audioUrl;

    private LocalDateTime creationDate;
    private LocalDateTime lastUpdatedDate;

    private LocalDateTime creationDateAudioTokenUrl;
    private LocalDateTime expirationDateAudioTokenUrl;

    private SoundType soundType;
    private Boolean liked;
}
