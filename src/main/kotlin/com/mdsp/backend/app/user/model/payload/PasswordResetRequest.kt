package com.mdsp.backend.app.user.model.payload

import javax.validation.constraints.NotBlank

class PasswordResetRequest {
    @NotBlank(message = "Password cannot be blank")
    private var password: String? = null

    @NotBlank(message = "Confirm Password cannot be blank")
    private var confirmPassword: String? = null

    @NotBlank(message = "Token has to be supplied along with a password reset request")
    private var token: String? = null

    fun PasswordResetRequest() {}

    fun PasswordResetRequest(password: String?, confirmPassword: String?, token: String?) {
        this.password = password
        this.confirmPassword = confirmPassword
        this.token = token
    }

    fun getConfirmPassword(): String? { return confirmPassword }
    fun setConfirmPassword(confirmPassword: String?) { this.confirmPassword = confirmPassword }

    fun getPassword(): String? { return password }
    fun setPassword(password: String?) { this.password = password }

    fun getToken(): String? { return token }

    fun setToken(token: String?) { this.token = token }
}
