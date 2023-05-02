package com.mdsp.backend.app.user.security.service

import com.mdsp.backend.app.user.event.listener.OnGenerateResetLinkEventListener
import com.mdsp.backend.app.user.exception.InvalidTokenRequestException
import com.mdsp.backend.app.user.model.TokenStatus
import com.mdsp.backend.app.user.model.token.EmailVerificationToken
import com.mdsp.backend.app.user.repository.EmailVerificationTokenRepository
import org.apache.logging.log4j.LogManager
//import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class EmailVerificationTokenService {
//    private val logger = Logger.getLogger(EmailVerificationTokenService::class.java)

    private var logger = LogManager.getLogger(OnGenerateResetLinkEventListener::class.java)

    @Autowired
    private var emailVerificationTokenRepository: EmailVerificationTokenRepository

    //@Value("\${app.token.email.verification.duration}")
    private val emailVerificationTokenExpiryDuration: Long = 3600000

    @Autowired
    constructor(emailVerificationTokenRepository: EmailVerificationTokenRepository) {
        this.emailVerificationTokenRepository = emailVerificationTokenRepository
    }

    fun createVerificationToken(profileId: UUID, token: String){
        var emailVerificationToken = EmailVerificationToken()
        emailVerificationToken.setToken(token)
        emailVerificationToken.setTokenStatus(TokenStatus.STATUS_PENDING)
        emailVerificationToken.setProfileId(profileId)
        emailVerificationToken.setExpiryDate(Instant.now().plusMillis(emailVerificationTokenExpiryDuration))
        logger.info("Generated Email verification token [$emailVerificationToken]")
        emailVerificationTokenRepository.save(emailVerificationToken)
    }

    fun updateExistingTokenWithNameAndExpiry(existingToken: EmailVerificationToken): EmailVerificationToken {
        existingToken.setTokenStatus(TokenStatus.STATUS_PENDING)
        existingToken.setExpiryDate(Instant.now().plusMillis(emailVerificationTokenExpiryDuration))
        logger.info("Updated Email verification token [$existingToken]")
        return save(existingToken)
    }

    fun findByToken(token: String): Optional<EmailVerificationToken> = emailVerificationTokenRepository.findByToken(token)

    fun save(emailVerificationToken: EmailVerificationToken): EmailVerificationToken
            = emailVerificationTokenRepository.save(emailVerificationToken)

    fun generateNewToken(): String = UUID.randomUUID().toString()

    fun verifyExpiration(token: EmailVerificationToken){
        if(token.getExpiryDate()!!.compareTo(Instant.now()) < 0)
            throw InvalidTokenRequestException("Email Verification Token", token.getToken(), "Expired token. Please issue a new request!")
    }

}
