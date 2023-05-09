package com.soundpro.sounds.services.exceptions;

public class ConvertMultipartFileToBytesException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    public ConvertMultipartFileToBytesException(String message){
        super(message);
    }
}
