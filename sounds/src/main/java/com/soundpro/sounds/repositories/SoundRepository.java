package com.soundpro.sounds.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.soundpro.sounds.models.Sound;

@Repository
public interface SoundRepository extends MongoRepository<Sound, String> {
    
}
