package com.soundpro.sounds.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.soundpro.sounds.models.Sound;

public interface SoundRepository extends MongoRepository<Sound, String> {

    @Query("{ 'name' : { $regex: ?0, $options: 'i' } }")
    Page<Sound> findSoundByName(String name, Pageable pageable);
    
}
