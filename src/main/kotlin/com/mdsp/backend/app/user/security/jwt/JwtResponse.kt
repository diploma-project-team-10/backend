package com.mdsp.backend.app.user.security.jwt

import org.springframework.security.core.GrantedAuthority

class JwtResponse(var accessToken: String?, var refreshToken: String?) {
}
