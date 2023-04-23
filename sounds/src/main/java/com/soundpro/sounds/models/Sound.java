package com.soundpro.sounds.models;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.soundpro.sounds.dtos.CategoryDTO;
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
    private SoundType soundType;
    private Integer duration;
    private Set<CategoryDTO> categories = new HashSet<>();
    private byte[] audio;
    private byte[] image;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime creationDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime lastUpdatedDate;
}
