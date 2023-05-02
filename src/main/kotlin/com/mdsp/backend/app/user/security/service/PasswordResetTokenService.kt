package com.mdsp.backend.app.user.security.service

import com.mdsp.backend.app.system.model.Util
import com.mdsp.backend.app.user.exception.InvalidTokenRequestException
import com.mdsp.backend.app.user.model.PasswordResetToken
import com.mdsp.backend.app.user.repository.PasswordResetTokenRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class PasswordResetTokenService {
    private var passwordResetTokenRepository: PasswordResetTokenRepository

    //@Value("app.token.password.reset.duration")
    private var expiration: Long = 3600000

    constructor(passwordResetTokenRepository: PasswordResetTokenRepository) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    fun save(passwordResetToken: PasswordResetToken): PasswordResetToken{
        return passwordResetTokenRepository.save(passwordResetToken)
    }

    fun findByToken(token: UUID): Optional<PasswordResetToken>{
        return passwordResetTokenRepository.findByToken(token)
    }

    fun createToken(): PasswordResetToken {
        val passwordResetToken = PasswordResetToken()
        val token: UUID = Util.generateRandomUuid()
        passwordResetToken.setToken(token)
        passwordResetToken.setExpiryDate(Instant.now().plusMillis(expiration))
        return passwordResetToken
    }

    fun verifyExpiration(token: PasswordResetToken) {
        if (token.getExpiryDate()!!.compareTo(Instant.now()) < 0) {
            throw InvalidTokenRequestException("Password Reset Token", token.getToken().toString(),
                    "Expired token. Please issue a new request")
        }
    }
}
