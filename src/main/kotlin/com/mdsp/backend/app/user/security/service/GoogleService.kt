package com.mdsp.backend.app.user.security.service

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.mdsp.backend.app.profile.repository.IProfileRepository
import com.mdsp.backend.app.user.config.GoogleConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import kotlin.String


@Service
class GoogleService {

    @Autowired
    lateinit var userRepository: IProfileRepository

    @Autowired
    lateinit var googleConfig: GoogleConfig

    private val transport: HttpTransport = NetHttpTransport()
    private val jsonFactory: JsonFactory = JacksonFactory()

    fun getProfileDetailByToken(idTokenString: String): GoogleIdToken.Payload? {
        val verifier = GoogleIdTokenVerifier.Builder(
            transport,
            jsonFactory
        )
            .setAudience(googleConfig.getAudience())
            .build()

        return verifier.verify(idTokenString)?.payload
    }

}
