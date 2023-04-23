package com.soundpro.sounds.dtos;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.soundpro.sounds.utils.Base64Util;

import com.soundpro.sounds.enums.SoundType;
import com.soundpro.sounds.models.Sound;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SoundDTO implements Serializable{

    private String id;
    private String name;
    private SoundType soundType;
    private Integer duration;
    private Set<CategoryDTO> categories = new HashSet<>();
    private byte[] audio;
    private byte[] image;

    public SoundDTO(Sound obj){
        id = obj.getId();
        name = obj.getName();
        soundType = obj.getSoundType();
        duration = obj.getDuration();
        categories = obj.getCategories();
        audio = obj.getAudio();
        image = obj.getImage();
        //audio = this.convertBytesToBase64(obj.getAudio());
        //image = this.convertBytesToBase64(obj.getImage());
    }

    public Sound converToEntity(SoundDTO dto){
        Sound entity = new Sound();
        entity.setName(dto.getName());
        entity.setSoundType(dto.getSoundType());
        entity.setDuration(dto.getDuration());
        entity.setCategories(dto.getCategories());
        entity.setAudio(dto.getAudio());
        entity.setImage(dto.getImage());
        //entity.setAudio(this.convertBase64ToBytes(dto.getAudio()));
        //entity.setImage(this.convertBase64ToBytes(dto.getImage()));
        return entity;
    }

    public String convertBytesToBase64(byte[] bytes){
        return Base64Util.convertBytesToBase64(bytes);
    }
    public byte[] convertBase64ToBytes(String base64){
        return Base64Util.convertBase64ToBytes(base64);
    }

    
}
