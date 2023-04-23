package com.soundpro.sounds.dtos;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.soundpro.sounds.enums.SoundType;
import com.soundpro.sounds.models.SoundTeste;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SoundTesteDTO implements Serializable{
    
    private static final long serialVersionUID = 1L;

    private String id;
    
    private byte[] audio;
    private String name;
    private SoundType soundType;
    private Integer duration;
    private Set<CategoryDTO> categories = new HashSet<>();

    public SoundTesteDTO(SoundTeste entity){
        id = entity.getId();
        audio = entity.getAudio();
        name = entity.getName();
        soundType = entity.getSoundType();
        duration = entity.getDuration();
        categories = entity.getCategories();
    }

    public SoundTeste convertToSoundTeste(SoundTesteDTO obj){
        SoundTeste entity = new SoundTeste();
        entity.setAudio(obj.getAudio());
        entity.setName(obj.getName());
        entity.setSoundType(obj.getName().toLowerCase().contains("mp3") ? SoundType.MP3 : SoundType.WAV);
        entity.setDuration(obj.getDuration());
        entity.setCategories(obj.getCategories());
        return entity;
    }


}
