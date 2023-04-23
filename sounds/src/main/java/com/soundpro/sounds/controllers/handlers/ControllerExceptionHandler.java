package com.soundpro.sounds.controllers.handlers;

import java.time.LocalDateTime;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.soundpro.sounds.dtos.CustomErrorDTO;
import com.soundpro.sounds.services.exceptions.ParseException;

@ControllerAdvice
public class ControllerExceptionHandler {
    
    @ExceptionHandler(ParseException.class)
    public ResponseEntity<CustomErrorDTO> resourceNotFound(ParseException e, HttpServletRequest request){
        HttpStatus status = HttpStatus.BAD_REQUEST;
        CustomErrorDTO error = new CustomErrorDTO(LocalDateTime.now(), status.value(), e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(error);
    }

    
}