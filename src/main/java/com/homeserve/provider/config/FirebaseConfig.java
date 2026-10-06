package com.homeserve.provider.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
@Slf4j
public class FirebaseConfig {

    // =========================================================
    // FIREBASE APP
    // =========================================================

    @Bean
    public FirebaseApp firebaseApp() throws IOException {

        /*
         * Prevent duplicate Firebase initialization.
         *
         * Useful during Spring context reloads/tests.
         */
        if (!FirebaseApp.getApps().isEmpty()) {

            log.info(
                    "Firebase already initialized. Using existing FirebaseApp."
            );

            return FirebaseApp.getInstance();
        }


        /*
         * Reads credentials from:
         *
         * GOOGLE_APPLICATION_CREDENTIALS
         *
         * Example:
         *
         * D:\\firebase\\homeserve-firebase-admin.json
         */
        GoogleCredentials credentials =
                GoogleCredentials
                        .getApplicationDefault();


        FirebaseOptions options =
                FirebaseOptions.builder()

                        .setCredentials(
                                credentials
                        )

                        .build();


        FirebaseApp app =
                FirebaseApp.initializeApp(
                        options
                );


        log.info(
                "========================================"
        );

        log.info(
                "Firebase Admin SDK initialized successfully"
        );

        log.info(
                "Firebase App Name: {}",
                app.getName()
        );

        log.info(
                "========================================"
        );


        return app;
    }


    // =========================================================
    // FIREBASE MESSAGING
    // =========================================================

    @Bean
    public FirebaseMessaging firebaseMessaging(
            FirebaseApp firebaseApp
    ) {

        return FirebaseMessaging.getInstance(
                firebaseApp
        );
    }
}