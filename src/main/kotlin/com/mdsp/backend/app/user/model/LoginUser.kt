package com.mdsp.backend.app.user.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

class LoginUser {

    @JsonProperty("username")
    var username: String? = null

    @JsonProperty("password")
    var password: String? = null

    constructor() {}

    constructor(username: String?, password: String?) {
        this.username = username
        this.password = password
    }
}
