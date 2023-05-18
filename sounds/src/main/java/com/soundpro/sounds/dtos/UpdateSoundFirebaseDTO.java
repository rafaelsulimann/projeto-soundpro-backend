package com.soundpro.sounds.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSoundFirebaseDTO {

    private String originalSoundName;
    private String newSoundName;
    
}
