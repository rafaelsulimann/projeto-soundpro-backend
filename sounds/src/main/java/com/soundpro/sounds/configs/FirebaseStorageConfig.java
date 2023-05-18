package com.soundpro.sounds.configs;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

@Configuration
public class FirebaseStorageConfig {

    @Value("${soundpro.firebase.storage.projectId}")
    private String projectId;

    @Value("${soundpro.firebase.storage.filePath}")
    private String filePath;

    @Bean
    public GoogleCredentials credentials() throws IOException {
        ClassPathResource resource = new ClassPathResource(this.filePath);
        GoogleCredentials credentials = GoogleCredentials.fromStream(resource.getInputStream());
        return credentials;
    }

    @Bean
    public StorageOptions storageOptions() throws IOException {
        StorageOptions storageOptions = StorageOptions.newBuilder()
                .setProjectId(this.projectId)
                .setCredentials(this.credentials())
                .build();
        return storageOptions;
    }

    @Bean
    public Storage storage() throws IOException {
        return this.storageOptions().getService();
    }

}
