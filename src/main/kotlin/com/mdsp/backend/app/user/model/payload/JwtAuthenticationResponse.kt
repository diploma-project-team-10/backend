package com.mdsp.backend.app.user.model.payload

import java.util.*

class JwtAuthenticationResponse {
    private var accessToken: String

    private var refreshToken: UUID

    private var tokenType: String

    private var expiryDuration: Long

    constructor(accessToken: String, refreshToken: UUID, expiryDuration: Long) {
        this.accessToken = accessToken
        this.refreshToken = refreshToken
        this.expiryDuration = expiryDuration
        tokenType = "Bearer "
    }

    fun getAccessToken(): String? { return accessToken }
    fun setAccessToken(accessToken: String?) { this.accessToken = accessToken!! }

    fun getTokenType(): String? { return tokenType }
    fun setTokenType(tokenType: String?) { this.tokenType = tokenType!! }

    fun getRefreshToken(): UUID? { return refreshToken }
    fun setRefreshToken(refreshToken: UUID?) { this.refreshToken = refreshToken!! }

    fun getExpiryDuration(): Long? { return expiryDuration }
    fun setExpiryDuration(expiryDuration: Long?) { this.expiryDuration = expiryDuration!! }
}