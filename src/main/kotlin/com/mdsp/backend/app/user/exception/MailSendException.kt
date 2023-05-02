package com.mdsp.backend.app.user.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.lang.RuntimeException

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
class MailSendException: RuntimeException {
    private var recipientAddress: String? = null
    private var message2: String? = null

    constructor(recipientAddress: String?, message2: String?): super(String.format("Error sending [%s] for user [%s]", message2, recipientAddress)) {
        this.recipientAddress = recipientAddress
        this.message2 = message2
    }
}