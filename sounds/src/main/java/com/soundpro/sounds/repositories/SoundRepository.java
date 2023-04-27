package com.soundpro.sounds.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.soundpro.sounds.models.Sound;

public interface SoundRepository extends MongoRepository<Sound, String> {
    
}
