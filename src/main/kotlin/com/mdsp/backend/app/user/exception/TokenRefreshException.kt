package com.mdsp.backend.app.user.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.util.*

@ResponseStatus(HttpStatus.EXPECTATION_FAILED)
class TokenRefreshException: RuntimeException {
    private var token: UUID
    private var tokenMessage: String

    constructor(token: UUID, tokenMessage: String): super(String.format("Couldn't refresh token for [%s]: [%s])", token, tokenMessage)) {
        this.token = token
        this.tokenMessage = tokenMessage
    }
}