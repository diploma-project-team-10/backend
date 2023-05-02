package com.mdsp.backend.app.user.repository

import com.mdsp.backend.app.user.model.token.EmailVerificationToken
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface EmailVerificationTokenRepository : JpaRepository<EmailVerificationToken, Long> {

    fun findByToken(token: String): Optional<EmailVerificationToken>
}