package com.mdsp.backend.app.user.model.payload

import javax.validation.constraints.NotBlank

class PasswordChangeRequest {
    @NotBlank(message = "Password cannot be blank")
    private var password: String? = null

    @NotBlank(message = "Confirm Password cannot be blank")
    private var confirmPassword: String? = null

    @NotBlank(message = "Token has to be supplied along with a password reset request")
    private var newPassword: String? = null

    constructor() {}

    constructor(password: String?, confirmPassword: String?, newPassword: String?) {
        this.password = password
        this.confirmPassword = confirmPassword
        this.newPassword = newPassword
    }

    fun getConfirmPassword(): String? { return confirmPassword }
    fun setConfirmPassword(confirmPassword: String?) { this.confirmPassword = confirmPassword }

    fun getPassword(): String? { return password }
    fun setPassword(password: String?) { this.password = password }

    fun getNewPassword(): String? { return newPassword }
    fun setNewPassword(token: String?) { this.newPassword = token }
}
