package com.ejemplo.microservicio.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Crea el cliente de AWS S3.
 *
 * Las credenciales se toman automaticamente del entorno: en tu cuenta de
 * laboratorio, las variables AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY y
 * AWS_SESSION_TOKEN (que pasaras al contenedor en el docker run).
 */
@Configuration
public class AwsConfig {

    @Value("${aws.region}")
    private String region;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}
