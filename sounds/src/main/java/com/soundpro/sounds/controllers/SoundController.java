package com.soundpro.sounds.controllers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.firebase.database.annotations.NotNull;
import com.soundpro.sounds.dtos.DownloadSoundDTO;
import com.soundpro.sounds.dtos.SoundDTO;
import com.soundpro.sounds.dtos.SoundUpdateRequestDTO;
import com.soundpro.sounds.dtos.YoutubeConverterDTO;
import com.soundpro.sounds.services.SoundService;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping(value = "/sounds")
public class SoundController {

    @Autowired
    private SoundService soundService;

    @GetMapping
    public ResponseEntity<Page<SoundDTO>> findAll(
            @PageableDefault(page = 0, size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(this.soundService.findAll(pageable));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SoundDTO> insertSound(@RequestPart("audio") @NotNull MultipartFile audio) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.soundService.insert(audio));
    }

    @DeleteMapping(value = "/{soundId}")
    public ResponseEntity<Object> deleteSound(@PathVariable String soundId){
        this.soundService.delete(soundId);
        return ResponseEntity.status(HttpStatus.OK).body("Sound deletado com sucesso");
    }

    @PutMapping(value = "/{soundId}")
    public ResponseEntity<SoundDTO> updateSound(@PathVariable String soundId, @RequestBody @Valid SoundUpdateRequestDTO soundUpdateRequestDTO){
        return ResponseEntity.status(HttpStatus.OK).body(this.soundService.update(soundId, soundUpdateRequestDTO));
    }

    @GetMapping(value = "/download/{soundId}")
    public ResponseEntity<InputStreamResource> downloadSound(@PathVariable String soundId){
        DownloadSoundDTO downloadDTO = this.soundService.findSoundBytesByName(soundId);
        InputStream inputStream = new ByteArrayInputStream(downloadDTO.getAudioContent());
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadDTO.getSoundName() + "\"");
        return ResponseEntity.status(HttpStatus.OK).headers(headers).contentType(MediaType.APPLICATION_OCTET_STREAM).body(new InputStreamResource(inputStream));
    }

    @PostMapping(value = "/converter")
    public ResponseEntity<SoundDTO> insrtSoundWithYoutubeUrl(@RequestBody @Valid YoutubeConverterDTO youtubeConverterDTO){
        return ResponseEntity.status(HttpStatus.OK).body(this.soundService.insertWithYoutubeUrl(youtubeConverterDTO));
    }

}
