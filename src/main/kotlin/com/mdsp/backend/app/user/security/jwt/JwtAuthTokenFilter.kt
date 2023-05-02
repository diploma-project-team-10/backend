package com.mdsp.backend.app.user.security.jwt

import com.amplitude.Amplitude
import com.amplitude.Event
import com.mdsp.backend.app.profile.service.ProfileService
import java.io.IOException

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.web.filter.OncePerRequestFilter

import com.mdsp.backend.app.user.security.service.UserDetailsServiceImpl
import org.springframework.beans.factory.annotation.Value

class JwtAuthTokenFilter : OncePerRequestFilter() {

    @Autowired
    private val tokenProvider: JwtProvider? = null

    @Autowired
    lateinit var profileService: ProfileService

    @Value("\${spring.application.name}")
    private var applicationPlatformName: String = "localhost"

    @Autowired
    private val userDetailsService: UserDetailsServiceImpl? = null

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val client: Amplitude = Amplitude.getInstance();
        client.init("5810e8ba76294a451c3798cf681cc70a");
        val event: Event = Event("${request.method}: ${request.requestURI}", "unauthorized", "deviceId")
        event.platform = applicationPlatformName
        event.osName = request.getHeader("user-agent")

        try {
            val jwt = getJwt(request)
            if (jwt != null && tokenProvider!!.validateJwtToken(jwt)) {
                val username = tokenProvider.getUserNameFromJwtToken(jwt)
                val userDetails = userDetailsService!!.loadUserByUsername(username)
                val authentication = UsernamePasswordAuthenticationToken(
                        userDetails,
                    null,
                    userDetails.authorities
                )
                authentication.details = WebAuthenticationDetailsSource().buildDetails(request)

                SecurityContextHolder.getContext().authentication = authentication
                val profileOptional = profileService.getProfileByUsernameOrEmail(authentication.name)
                if(profileOptional.isPresent){
                    val profile = profileOptional.get()
                    event.userId = "ID: ${profile.getId()} username: ${profile.getUsername()}"
                }
            }
        } catch (e: Exception) {
            logger.error("Can NOT set user authentication -> Message: {}", e)
        }

        filterChain.doFilter(request, response)
        client.logEvent(event)
    }

    private fun getJwt(request: HttpServletRequest): String? {
        val authHeader = request.getHeader("Authorization")

        return if (authHeader != null && authHeader.startsWith("Bearer ")) {
            authHeader.replace("Bearer ", "")
        } else null
    }

    companion object {
        private val logger = LoggerFactory.getLogger(JwtAuthTokenFilter::class.java)
    }
}
