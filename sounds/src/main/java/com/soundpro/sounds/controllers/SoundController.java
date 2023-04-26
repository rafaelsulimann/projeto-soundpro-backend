package com.soundpro.sounds.controllers;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.soundpro.sounds.dtos.SoundTesteFirebaseDTO;
import com.soundpro.sounds.services.SoundService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping(value = "/sounds")
public class SoundController {

    @Autowired
    private SoundService soundService;

    @GetMapping
    public ResponseEntity<Page<SoundTesteFirebaseDTO>> findAll(
            @PageableDefault(page = 0, size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(soundService.findAll(pageable));
    }

    //@PostMapping
    //public ResponseEntity<SoundDTO> insertSound(@RequestBody SoundDTO soundDTO) {
    //log.debug("POST insertSound soundDTO received", soundDTO.toString());
    //return
    //ResponseEntity.status(HttpStatus.CREATED).body(soundService.insertSound(soundDTO));
    //}
    //@PostMapping
    //public ResponseEntity<SoundTesteFirebaseDTO> insertSound(@RequestBody SoundTesteFirebaseDTO soundDTO) {
    //log.debug("POST insertSound soundDTO received", soundDTO.toString());
    //return
    //ResponseEntity.status(HttpStatus.CREATED).body(soundService.insertSound3(soundDTO));
    //}

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @CrossOrigin(origins = "", allowedHeaders = "")
    public ResponseEntity<SoundTesteFirebaseDTO> insertSound(@RequestPart("name") String name, @RequestPart("audio") MultipartFile audio) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED).body(soundService.insertSound4(name, audio));
    }

}
