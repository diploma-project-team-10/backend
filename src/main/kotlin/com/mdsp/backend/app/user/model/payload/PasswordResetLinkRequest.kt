package com.mdsp.backend.app.user.model.payload

import javax.validation.constraints.NotBlank

class PasswordResetLinkRequest {

    @NotBlank(message = "Email cannot be blank")
    private var email: String? = null

    fun PasswordResetLinkRequest(email: String?) { this.email = email }
    fun PasswordResetLinkRequest() {}

    fun getEmail(): String? { return email }
    fun setEmail(email: String?) { this.email = email }
}