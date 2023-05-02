package com.mdsp.backend.app.user.event

import com.mdsp.backend.app.profile.model.Profile
import org.springframework.context.ApplicationEvent
import org.springframework.web.util.UriComponentsBuilder

class OnUserRegistrationCompleteEvent: ApplicationEvent {
    @Transient
    private var redirectUrl: UriComponentsBuilder? = null
    private var user: Profile? = null

    constructor(user: Profile, redirectUrl: UriComponentsBuilder): super(user) {
        this.user = user
        this.redirectUrl = redirectUrl
    }

    fun getRedirectUrl() = redirectUrl
    fun setRedirectUrl(redirectUrl: UriComponentsBuilder?) { this.redirectUrl = redirectUrl }

    fun getUser() = user
    fun setUser(user: Profile?) { this.user = user }
}