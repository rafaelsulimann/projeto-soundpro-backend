package com.soundpro.sounds.dtos;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class YoutubeConverterDTO implements Serializable{

    private static final long serialVersionUID = 1L;

    @NotBlank
    private String youtubeVideoUrl;

    @NotBlank
    private String requestId;
    
}
