package com.soundpro.sounds.dtos;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoadingFileDTO implements Serializable{

    private String soundName;
    private Integer timeLeft;
    private Integer progressPercentage;
    
}
