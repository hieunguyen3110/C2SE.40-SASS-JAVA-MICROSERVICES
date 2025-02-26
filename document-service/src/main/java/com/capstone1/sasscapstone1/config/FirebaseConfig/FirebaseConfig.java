package com.capstone1.sasscapstone1.config.FirebaseConfig;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.bucket-name}")
    private String bucketName;

    @Bean
    public String initFirebase() throws Exception {
        try{
            System.out.println("Connecting to firebase");
            ClassPathResource serviceAccount= new ClassPathResource("serviceAccountKey.json");
            FirebaseOptions options= new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount.getInputStream()))
                    .setStorageBucket(bucketName)
                    .build();
            FirebaseApp.initializeApp(options);
            return "finished connect to firebase";
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }
}
