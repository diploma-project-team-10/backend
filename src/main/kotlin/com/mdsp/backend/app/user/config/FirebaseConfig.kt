package com.mdsp.backend.app.user.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.FirestoreOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import java.io.FileInputStream
import java.io.IOException

@Configuration
class FirebaseConfig {
    @Value("\${firebase.config.path}")
    val configPath: String? = null

    @Primary
    @Bean
    @Throws(IOException::class)
    fun getFirebaseApp(): FirebaseApp? {
        val serviceAccount = FileInputStream(configPath)

        val options = FirebaseOptions.builder().setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .build()
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options)
        }
        return FirebaseApp.getInstance()
    }

    @Bean
    @Throws(IOException::class)
    fun getAuth(): FirebaseAuth? {
        return FirebaseAuth.getInstance(getFirebaseApp())
    }

    @Bean
    @Throws(IOException::class)
    fun getMessaging(): FirebaseMessaging? {
        return FirebaseMessaging.getInstance(getFirebaseApp())
    }

    @Bean
    @Throws(IOException::class)
    fun getRemoteConfig(): FirebaseRemoteConfig? {
        return FirebaseRemoteConfig.getInstance(getFirebaseApp())
    }
}
