package com.soundpro.sounds.models;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.soundpro.sounds.dtos.CategoryDTO;
import com.soundpro.sounds.enums.SoundType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "sounds-teste")
public class SoundTeste implements Serializable{

    private static final long serialVersionUID = 1L;

    @Id
    public String id;

    private byte[] audio;
    private String name;
    private SoundType soundType;
    private Integer duration;
    private Set<CategoryDTO> categories = new HashSet<>();
    
}
