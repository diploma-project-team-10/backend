package com.mdsp.backend.app.user.model.payload

class RegistrationRequest {
    private var username: String? = null
    private var email: String? = null
    private var password: String? = null
    private var registerAsAdmin: Boolean? = null

    constructor(username: String, email: String,
                password: String, registerAsAdmin: Boolean) {
        this.username = username
        this.email = email
        this.password = password
        this.registerAsAdmin = registerAsAdmin
    }

    fun constructor() {}

    fun getUsername() = username
    fun setUsername(username: String?) { this.username = username }

    fun getEmail() = email
    fun setEmail(email: String?) { this.email = email }

    fun getPassword() = password
    fun setPassword(password: String?) { this.password = password }

    fun getRegisterAsAdmin() = registerAsAdmin
    fun setRegisterAsAdmin(registerAsAdmin: Boolean?) { this.registerAsAdmin = registerAsAdmin }
}