package com.mdsp.backend.app.user.event

import com.mdsp.backend.app.user.model.token.EmailVerificationToken
import org.springframework.context.ApplicationEvent
import org.springframework.web.util.UriComponentsBuilder
import java.util.*

class OnRegenerateEmailVerificationEvent: ApplicationEvent {
    @Transient
    private var redirectUrl: UriComponentsBuilder? = null
    private var profileId: UUID? = null

    @Transient
    private var token: EmailVerificationToken? = null

    constructor(profileId: UUID, redirectUrl: UriComponentsBuilder, token: EmailVerificationToken): super(profileId) {
        this.profileId = profileId
        this.redirectUrl = redirectUrl
        this.token = token
    }

    fun getRedirectUrl() = redirectUrl
    fun setRedirectUrl(redirectUrl: UriComponentsBuilder?) { this.redirectUrl = redirectUrl }

    fun getProfileId() = profileId
    fun setProfileId(profileId: UUID) { this.profileId = profileId }

    fun getToken() = token
    fun setToken(token: EmailVerificationToken?) { this.token = token }

}