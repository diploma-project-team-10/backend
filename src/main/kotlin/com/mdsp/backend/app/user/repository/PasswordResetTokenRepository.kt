package com.mdsp.backend.app.user.repository

import com.mdsp.backend.app.user.model.PasswordResetToken
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*
import javax.transaction.Transactional

interface PasswordResetTokenRepository: JpaRepository<PasswordResetToken, Long> {
    fun findByToken(token: UUID): Optional<PasswordResetToken>

    fun findByProfileId(profileId: UUID): Optional<PasswordResetToken>

    @Transactional
    fun deleteAllByProfileId(profileId: UUID)

}
