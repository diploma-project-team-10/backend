package com.mdsp.backend.app.user.model

import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import java.util.*

class UserPrincipal(user: UserDetails, val id: UUID) : User(
    user.username,
    user.password,
    user.isEnabled,
    user.isAccountNonExpired,
    user.isCredentialsNonExpired,
    user.isAccountNonLocked,
    user.authorities
) {

    //  (authentication.principal as UserPrincipal).getId()
}
