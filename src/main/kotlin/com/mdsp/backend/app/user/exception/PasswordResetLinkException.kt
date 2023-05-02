package com.mdsp.backend.app.user.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.EXPECTATION_FAILED)
class PasswordResetLinkException: RuntimeException{
    private var user: String? = null
    private var message2: String? = null

    constructor(user: String?, message2: String?): super(String.format("Failed to send password reset link to User[%s] : '%s'", user, message2)) {
        this.user = user
        this.message2 = message2
    }
}