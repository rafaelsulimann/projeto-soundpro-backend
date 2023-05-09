package com.soundpro.sounds.listeners;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.adapter.ConsumerRecordMetadata;
import org.springframework.stereotype.Component;

import com.soundpro.sounds.services.FirebaseStorageService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class DeleteSoundListener {
    
    @Autowired
    private FirebaseStorageService firebaseStorageService;

    @KafkaListener(topics = "sound-delete-firebase", groupId = "sound-delete-firebase-group")
    public void listen(String message, ConsumerRecordMetadata metadata){
        log.info("Topic {}, Pt {}, Offset {} : {}", metadata.topic(), metadata.partition(), metadata.offset(), message);
        log.info("Timestamp {}", LocalDateTime.ofInstant(Instant.ofEpochMilli(metadata.timestamp()), TimeZone.getDefault().toZoneId()));
    }                                                                                   
}
