package com.mdsp.backend.app.user.model.payload

import javax.validation.constraints.NotNull

class User {
    private var username: String? = null

    private lateinit var password: String

    private var providerId: String? = "local"

    private var deviceId: String? = null

    private var rememberMe: Boolean? = false

    private var idToken: String? = null

    constructor() {}

    constructor(username: String?, password: String) {
        setUsername(username)
        this.password = password
    }

    fun getUsername(): String? { return username }
    fun setUsername(username: String?) {
        this.username = username
        if (username != null) {
            this.username = username.trim()
        }
    }

    fun getPassword(): String? { return password }
    fun setPassword(password: String?) { this.password = password!! }

    fun getProviderId(): String? = providerId
    fun setProviderId(providerId: String?) {
        this.providerId = providerId
    }

    fun getDeviceId(): String? = deviceId
    fun setDeviceId(deviceId: String?) {
        this.deviceId = deviceId
    }

    fun getRememberMe(): Boolean? = rememberMe
    fun setRememberMe(rememberMe: Boolean?) {
        this.rememberMe = rememberMe
    }

    fun getIdToken(): String? = idToken
    fun setIdToken(idToken: String?) {
        this.idToken = idToken
    }
}
