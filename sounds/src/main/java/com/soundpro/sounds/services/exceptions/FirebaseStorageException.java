package com.soundpro.sounds.services.exceptions;

public class FirebaseStorageException extends RuntimeException{

    private static final long serialVersionUID = 1L;

    public FirebaseStorageException(String msg){
        super(msg);
    }
    
}
