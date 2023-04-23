package com.soundpro.sounds.services;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soundpro.sounds.dtos.CategoryDTO;
import com.soundpro.sounds.dtos.SoundDTO;
import com.soundpro.sounds.dtos.SoundTesteDTO;
import com.soundpro.sounds.models.Sound;
import com.soundpro.sounds.models.SoundTeste;
import com.soundpro.sounds.repositories.SoundRepository;
import com.soundpro.sounds.repositories.SoundTesteRepository;
import com.soundpro.sounds.services.exceptions.ParseException;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class SoundService {

    @Autowired
    private SoundRepository soundRepository;

    @Autowired
    private SoundTesteRepository soundTesteRepository;

    //public Page<SoundDTO> findAll(Pageable pageable) {
    //    return soundRepository.findAll(pageable).map(sound -> new SoundDTO(sound));
    //}
    public Page<SoundTesteDTO> findAll(Pageable pageable) {
        return soundTesteRepository.findAll(pageable).map(sound -> new SoundTesteDTO(sound));
    }

    public SoundDTO insertSound(SoundDTO soundDTO) {
        Sound entity = soundDTO.converToEntity(soundDTO);
        entity.setCreationDate(LocalDateTime.now(ZoneId.of("UTC")));
        entity.setLastUpdatedDate(LocalDateTime.now(ZoneId.of("UTC")));
        entity = soundRepository.insert(entity);
        log.debug("POST insertSound sound saved {} ", entity.toString());
        log.info("Sound saved successfully sound {} ", entity.toString());
        return new SoundDTO(entity);
    }

    // public SoundTesteDTO insertSoundTeste(SoundTesteDTO soundTesteDTO){
    // SoundTeste entity = new SoundTeste(null, soundTesteDTO.getAudio());
    // entity = soundTesteRepository.insert(entity);
    // return new SoundTesteDTO(entity.getId(), entity.getAudio());
    //
    // }
    public SoundTesteDTO insertSoundTeste2(String name, String durationSTR, String categoriesSTR, byte[] audio) {

        double valor = Double.parseDouble(durationSTR);
        int duration = (int) Math.round(valor);
        ObjectMapper objectMapper = new ObjectMapper();
        JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, CategoryDTO.class);
        List<CategoryDTO> categories = null;

        try {
            categories = objectMapper.readValue(categoriesSTR, type);
            
            SoundTesteDTO dto = new SoundTesteDTO();
            dto.setName(name);
            dto.setAudio(audio);
            dto.setDuration(duration);
            dto.setCategories(new HashSet<>(categories));
            
            SoundTeste entity = dto.convertToSoundTeste(dto);
            
            entity = soundTesteRepository.insert(entity);
            return new SoundTesteDTO(entity);
        } 

        catch (JsonProcessingException e) {
            throw new ParseException("Erro ao realizar o parse das categorias");
        }

    }

}
