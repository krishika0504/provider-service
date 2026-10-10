package com.homeserve.provider.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.project-id}")
    private String firebaseProjectId;


    @Bean
    public FirebaseApp firebaseApp()
            throws IOException {

        GoogleCredentials credentials =
                GoogleCredentials
                        .getApplicationDefault();


        FirebaseOptions options =
                FirebaseOptions
                        .builder()

                        .setCredentials(
                                credentials
                        )

                        .setProjectId(
                                firebaseProjectId
                        )

                        .build();


        FirebaseApp firebaseApp;

        if (FirebaseApp.getApps().isEmpty()) {

            firebaseApp =
                    FirebaseApp.initializeApp(
                            options
                    );

        } else {

            firebaseApp =
                    FirebaseApp.getInstance();
        }


        log.info(
                "Firebase Admin SDK initialized successfully"
        );

        log.info(
                "Firebase App Name: {}",
                firebaseApp.getName()
        );

        log.info(
                "Firebase Project ID: {}",
                firebaseApp
                        .getOptions()
                        .getProjectId()
        );


        return firebaseApp;
    }


    @Bean
    public FirebaseMessaging firebaseMessaging(
            FirebaseApp firebaseApp
    ) {

        return FirebaseMessaging
                .getInstance(
                        firebaseApp
                );
    }
}