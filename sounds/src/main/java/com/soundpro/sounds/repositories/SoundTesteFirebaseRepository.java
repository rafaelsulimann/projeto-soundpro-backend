package com.soundpro.sounds.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.soundpro.sounds.models.SoundTesteFirebase;

@Repository
public interface SoundTesteFirebaseRepository extends MongoRepository<SoundTesteFirebase, String>{
    
}
