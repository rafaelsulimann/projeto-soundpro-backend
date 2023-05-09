package com.soundpro.sounds.dtos;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FirebaseAudioDTO {

    private String signedUrl;
    private LocalDateTime creationDateAudioUrlToken;
    private LocalDateTime expirationDateAudioUrlToken;
    
}
