package com.soundpro.sounds.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.soundpro.sounds.models.SoundTeste;

@Repository
public interface SoundTesteRepository extends MongoRepository<SoundTeste, String>{
    
}
