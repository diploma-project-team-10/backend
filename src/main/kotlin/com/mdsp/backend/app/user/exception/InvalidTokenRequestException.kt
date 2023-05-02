package com.mdsp.backend.app.user.exception

import java.util.*

class InvalidTokenRequestException: RuntimeException {
    private var tokenType: String? = null
    private var token: String? = null
    private var message2: String? = null

    constructor(tokenType: String?, token: String?, message2: String?): super(String.format("%s: [%s] token: [%s] ", message2, tokenType, token)) {
        this.tokenType = tokenType
        this.token = token
        this.message2 = message2
    }
}