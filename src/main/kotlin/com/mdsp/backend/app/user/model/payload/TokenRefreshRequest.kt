package com.mdsp.backend.app.user.model.payload

import java.util.*
import javax.validation.constraints.NotBlank

class TokenRefreshRequest {
//    @NotBlank(message = "Refresh token cannot be blank")
    private var refreshToken: UUID? = null

    constructor() {}

    constructor(refreshToken: UUID){
        this.refreshToken = refreshToken
    }

    fun getRefreshToken() = refreshToken

    fun setRefreshToken(refreshToken: UUID){
        this.refreshToken = refreshToken
    }
}
