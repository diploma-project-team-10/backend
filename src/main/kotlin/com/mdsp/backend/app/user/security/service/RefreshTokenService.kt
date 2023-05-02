package com.mdsp.backend.app.user.security.service

import com.mdsp.backend.app.system.model.Util
import com.mdsp.backend.app.user.exception.TokenRefreshException
import com.mdsp.backend.app.user.model.token.RefreshToken
import com.mdsp.backend.app.user.repository.RefreshTokenRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class RefreshTokenService {
    private var refreshTokenRepository: RefreshTokenRepository

    @Value("\${app.token.refresh.duration}")
    private var refreshTokenDurationMs: Long = 0

    @Autowired
    constructor(refreshTokenRepository: RefreshTokenRepository){
        this.refreshTokenRepository = refreshTokenRepository
    }

    fun findByToken(token: UUID): Optional<RefreshToken> { return refreshTokenRepository.findByToken(token)}

    fun save(refreshToken: RefreshToken): RefreshToken { return refreshTokenRepository.save(refreshToken) }

    fun createRefreshToken(): RefreshToken {
        val refreshToken = RefreshToken()
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
        refreshToken.setToken(Util.generateRandomUuid())
        refreshToken.setRefreshCount(0L)
        return refreshToken
    }

    fun verifyExpiration(token: RefreshToken): Boolean {
        println("expired")
        println(token.getExpiryDate().compareTo(Instant.now()))
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
//            throw TokenRefreshException(token.getToken(), "Expired token. Please issue a new request")
            return false
        }
        return true
    }

    fun deleteById(id: UUID) { refreshTokenRepository.deleteById(id) }

    fun increaseCount(refreshToken: RefreshToken) {
        refreshToken.incrementRefreshCount()
        save(refreshToken)
    }
}
