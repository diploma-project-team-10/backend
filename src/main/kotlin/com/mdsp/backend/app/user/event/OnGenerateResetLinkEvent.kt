package com.mdsp.backend.app.user.event

import com.mdsp.backend.app.user.model.PasswordResetToken
import org.springframework.context.ApplicationEvent
import org.springframework.web.util.UriComponentsBuilder

class OnGenerateResetLinkEvent: ApplicationEvent {
    @Transient
    private var redirectUrl: UriComponentsBuilder? = null

    @Transient
    private var passwordResetToken: PasswordResetToken? = null

    constructor(passwordResetToken: PasswordResetToken, redirectUrl: UriComponentsBuilder): super(passwordResetToken) {
        this.passwordResetToken = passwordResetToken
        this.redirectUrl = redirectUrl
    }

    fun getPasswordResetToken(): PasswordResetToken? { return passwordResetToken }
    fun setPasswordResetToken(passwordResetToken: PasswordResetToken?) { this.passwordResetToken = passwordResetToken }

    fun getRedirectUrl(): UriComponentsBuilder? { return redirectUrl }
    fun setRedirectUrl(redirectUrl: UriComponentsBuilder?) { this.redirectUrl = redirectUrl }
}