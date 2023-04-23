package com.soundpro.sounds.utils;

import java.util.Base64;

public class Base64Util {

    public static byte[] convertBase64ToBytes(String base64String) {
        return Base64.getDecoder().decode(base64String);
    }

    public static String convertBytesToBase64(byte[] bytes){
        return Base64.getEncoder().encodeToString(bytes);
    }
    
}
