package com.soundpro.sounds.configs;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfigTeste implements WebMvcConfigurer{

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.stream()
                .filter(converter -> converter instanceof ByteArrayHttpMessageConverter)
                .forEach(converter -> ((ByteArrayHttpMessageConverter) converter).setSupportedMediaTypes(
                        Arrays.asList(MediaType.APPLICATION_OCTET_STREAM, MediaType.ALL)));
    }
    
}
