package com.soundpro.sounds.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DownloadSoundDTO {

    private String soundName;
    private byte[] audioContent;
    
}
