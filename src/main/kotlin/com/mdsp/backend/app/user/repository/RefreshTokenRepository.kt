package com.mdsp.backend.app.user.repository

import com.mdsp.backend.app.user.model.token.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.*
import javax.transaction.Transactional

interface RefreshTokenRepository: JpaRepository<RefreshToken, Long> {
    fun findById(id: UUID): Optional<RefreshToken>

    fun findAllByProfileId(@Param ("profileId") profileId: UUID): ArrayList<RefreshToken>

    fun findByToken(token: UUID): Optional<RefreshToken>

    fun findByProfileIdAndToken(profileId: UUID, token: UUID): Optional<RefreshToken>

    fun findByFromUsedToken(@Param ("fromUsedToken") fromUsedToken: UUID): Optional<RefreshToken>

    @Transactional
    fun deleteById(@Param("id") id: UUID)

    @Transactional
    fun deleteAllByProfileId(@Param("profileId") profileId: UUID)

    @Transactional
    fun deleteByToken(@Param("token") token: UUID)

    @Transactional
    fun deleteAllByExpiryDateLessThan(@Param("expiry_dt") expiry_dt: Instant)
}
