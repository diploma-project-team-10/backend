package com.mdsp.backend.app.user.model.payload

import javax.validation.constraints.NotBlank

class UserAuthRequest {
    var username: String? = null
    var email: String? = null
    var password: String? = null
    var confirmPassword: String? = null
    var newPassword: String? = null


}
