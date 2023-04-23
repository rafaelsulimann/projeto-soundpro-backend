package com.soundpro.sounds.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/teste")
public class TesteController {

    @GetMapping
    public String teste(){
        return "teste";
    }
    
}
