package com.mdsp.backend.app.user.controller

import com.amplitude.Amplitude
import com.amplitude.Event
import com.google.firebase.auth.FirebaseAuth
import com.mdsp.backend.app.mobile.repository.DeviceMobileRepository
import com.mdsp.backend.app.profile.repository.IProfileRepository
import com.mdsp.backend.app.profile.service.ProfileService
import com.mdsp.backend.app.structure.service.RolesGroupService
import com.mdsp.backend.app.system.model.Status
import com.mdsp.backend.app.user.event.OnGenerateResetLinkEvent
import com.mdsp.backend.app.user.event.OnRegenerateEmailVerificationEvent
import com.mdsp.backend.app.user.event.OnUserAccountChangeEvent
import com.mdsp.backend.app.user.event.OnUserRegistrationCompleteEvent
import com.mdsp.backend.app.user.exception.InvalidTokenRequestException
import com.mdsp.backend.app.user.exception.PasswordResetException
import com.mdsp.backend.app.user.exception.UserRegistrationException
import com.mdsp.backend.app.user.model.UserPrincipal
import com.mdsp.backend.app.user.model.payload.*
import com.mdsp.backend.app.user.model.token.EmailVerificationToken
import com.mdsp.backend.app.user.model.token.RefreshToken
import com.mdsp.backend.app.user.repository.EmailVerificationTokenRepository
import com.mdsp.backend.app.user.repository.PasswordResetTokenRepository
import com.mdsp.backend.app.user.repository.RefreshTokenRepository
import com.mdsp.backend.app.user.security.jwt.JwtProvider
import com.mdsp.backend.app.user.security.service.AuthService
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import org.springframework.web.util.UriComponentsBuilder
import java.util.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/auth")
class AuthController() {
    private val logger = LogManager.getLogger(AuthController::class.java)

    @Autowired
    private lateinit var authService: AuthService

    @Autowired
    private lateinit var tokenProvider: JwtProvider

    @Autowired
    private lateinit var refreshTokenRepository: RefreshTokenRepository

    @Autowired
    private lateinit var profileRepository: IProfileRepository

    @Autowired
    private lateinit var  applicationEventPublisher: ApplicationEventPublisher

    @Autowired
    private lateinit var  passwordResetTokenRepository: PasswordResetTokenRepository

    @Autowired
    private lateinit var  emailVerificationTokenRepository: EmailVerificationTokenRepository

    @Autowired
    private lateinit var rolesGroupService: RolesGroupService

    @Autowired
    private lateinit var profileService: ProfileService

    @Autowired
    private lateinit var encoder: PasswordEncoder

    @Autowired
    lateinit var deviceMobileRepository: DeviceMobileRepository

    @Value("\${frontend.scheme}")
    var scheme: String = ""

    @Value("\${frontend.host}")
    var frontHost: String = ""

    @GetMapping("/switch/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun authenticateAdminSwitch(
        @PathVariable(value = "id") id: UUID,
        @RequestHeader headers: Map<String, String>
    ): ResponseEntity<*> {
        println(headers)
        println("switch")
        val profile = profileRepository.findByIdAndDeletedAtIsNull(id)
        if (profile.isPresent) {
            val switchRequest = User()
            switchRequest.setUsername(profile.get().getUsername())
            return ResponseEntity.ok(authService.authenticate(switchRequest,  "secret", false))
        }
        val status = Status()
        status.message = "User not found!"
        return ResponseEntity.ok(status)
    }

    @PostMapping("/{typeSign}")
    fun authenticateUserLogin(
        @PathVariable(value = "typeSign") typeSign: String,
        @Valid @RequestBody loginRequest: User,
        @RequestHeader headers: Map<String, String>
    ): ResponseEntity<*> {
        println(headers)
        val client: Amplitude = Amplitude.getInstance();
        client.init("5810e8ba76294a451c3798cf681cc70a");
        client.logEvent(Event("User Sign in", "Nurzhan"))
        val secret = if (typeSign == "signin-auth") {
            "secret"
        } else {
            "default"
        }
        return ResponseEntity.ok(authService.authenticate(loginRequest, secret))
    }

    @PostMapping("/refresh")
    fun refreshJwtToken(@Valid @RequestBody tokenRefreshRequest: TokenRefreshRequest): ResponseEntity<*> {
        val status = Status()
        status.status = 0
        status.message = "Unexpected error during token refresh. Please logout and login again."

        val usedToken = refreshTokenRepository.findByFromUsedToken(tokenRefreshRequest.getRefreshToken()!!)
        if(usedToken.isPresent) {
            refreshTokenRepository.deleteAllByProfileId(usedToken.get().getProfileId()!!)
            return ResponseEntity(status, HttpStatus.OK)
        }
        val refreshTokenCandidate = refreshTokenRepository.findByToken(tokenRefreshRequest.getRefreshToken()!!)
        if(!refreshTokenCandidate.isPresent){
            return ResponseEntity(status, HttpStatus.OK)
        }

        val updatedToken = authService.refreshJwtToken(tokenRefreshRequest)
        if(updatedToken.get().equals("null")){
            return ResponseEntity(status, HttpStatus.OK)
        }
        val profileCandidate = profileRepository.findByIdAndDeletedAtIsNull(refreshTokenCandidate.get().getProfileId()!!)
        if(!profileCandidate.isPresent) {
            return ResponseEntity(status, HttpStatus.OK)
        }
        val refreshTokenOptional = authService.updateAndPersistRefreshToken(profileCandidate.get().getUsername()!!, refreshTokenCandidate.get().getToken())
        val refreshToken = refreshTokenOptional.get().getToken()
        status.status = 1
        status.message = "Great"
        status.value = JwtAuthenticationResponse(updatedToken.get(), refreshToken, tokenProvider.getExpiryDuration())
        return ResponseEntity.ok(status)
    }

    @PostMapping("/refresh-auth")
    fun refreshJwtTokenAuth(@Valid @RequestBody tokenRefreshRequest: TokenRefreshRequest): ResponseEntity<*>{
        val status = Status()
        status.status = 0
        status.message = "Unexpected error during token refresh. Please logout and login again."

        val usedToken = refreshTokenRepository.findByFromUsedToken(tokenRefreshRequest.getRefreshToken()!!)
        if(usedToken.isPresent) {
            refreshTokenRepository.deleteAllByProfileId(usedToken.get().getProfileId()!!)
            return ResponseEntity(status, HttpStatus.OK)
        }
        val refreshTokenCandidate = refreshTokenRepository.findByToken(tokenRefreshRequest.getRefreshToken()!!)
        if(!refreshTokenCandidate.isPresent){
            return ResponseEntity(status, HttpStatus.OK)
        }
        val updatedToken = authService.refreshJwtToken(tokenRefreshRequest)
        if(!updatedToken.isPresent){
            return ResponseEntity(status, HttpStatus.OK)
        }
        val profileCandidate = profileRepository.findByIdAndDeletedAtIsNull(refreshTokenCandidate.get().getProfileId()!!)
        if(!profileCandidate.isPresent) {
            return ResponseEntity(status, HttpStatus.OK)
        }
        val refreshTokenOptional = authService.updateAndPersistRefreshToken(profileCandidate.get().getUsername()!!, refreshTokenCandidate.get().getToken())
        val refreshToken = refreshTokenOptional.get().getToken()
        return ResponseEntity.ok(JwtAuthenticationSecretResponse(updatedToken.get(), refreshToken, tokenProvider.getExpiryDuration()))
    }

    @DeleteMapping("/deleterefresh")
    //@PreAuthorize("isAuthenticated()")
    fun deleteRefresh(@Valid @RequestBody refreshToken: RefreshToken): ResponseEntity<*>{
        refreshTokenRepository.deleteByToken(refreshToken.getToken())
        return ResponseEntity.ok("Deleted!")
    }

    @PostMapping("/password/resetlink")
    fun resetLink(@Valid @RequestBody passwordResetLinkRequest: PasswordResetLinkRequest): ResponseEntity<*>{
        val status = Status()
        status.status = 0
        status.message = ""
        val profileCandidate = profileRepository.findByEmailAndDeletedAtIsNull(passwordResetLinkRequest.getEmail()!!)
        if(!profileCandidate.isPresent) { return ResponseEntity("Email does not exist!", HttpStatus.BAD_REQUEST)}
        val existedLink = passwordResetTokenRepository.findByProfileId(profileCandidate.get().getId()!!)
        if(existedLink.isPresent) {
            var leftTimeMs = System.currentTimeMillis() - existedLink.get().createdAt.time
            if (leftTimeMs < 60000) {
                status.message = "You already send reset link for your email, try after ${leftTimeMs/1000} seconds please!"
                return ResponseEntity(status, HttpStatus.BAD_REQUEST)
            }
        }
        passwordResetTokenRepository.deleteAllByProfileId(profileCandidate.get().getId()!!)
        val passwordResetToken = authService.generatePasswordResetToken(passwordResetLinkRequest)

        val urlBuilder: UriComponentsBuilder = ServletUriComponentsBuilder.newInstance().scheme(this.scheme)
                .host(this.frontHost).pathSegment("#", "public", "reset-password")
        //println(urlBuilder.toUriString())
        val generateResetLinkMailEvent = OnGenerateResetLinkEvent(passwordResetToken.get(), urlBuilder)
//      com/mdsp/backend/app/user/event/listener/OnGenerateResetLinkEventListener.kt - query token
        applicationEventPublisher.publishEvent(generateResetLinkMailEvent)
        status.message = "Password reset link sent successfully"
        status.status = 1
        return ResponseEntity.ok(status)
    }

    @PostMapping("/password/reset")
    fun resetPassword(@Valid @RequestBody passwordResetRequest: PasswordResetRequest): ResponseEntity<*> {
        val status = Status()
        status.status = 0
        status.message = ""
        val changeProfile = authService.resetPassword(passwordResetRequest)
        if(!changeProfile.isPresent) { throw PasswordResetException(passwordResetRequest.getToken(), "Error in resetting password") }

        val onPasswordChangeEvent = OnUserAccountChangeEvent(changeProfile.get(), "Reset Password", "Changed Successfully")
        applicationEventPublisher.publishEvent(onPasswordChangeEvent)

        val pwdResetTokens = passwordResetTokenRepository.deleteAllByProfileId(changeProfile.get())
        status.status = 1
        status.message = "Password changed successfully"
        return ResponseEntity.ok(status)
    }

    @PostMapping("/password/change")
    @PreAuthorize("isAuthenticated()")
    fun changePassword(@Valid @RequestBody passwordChangeRequest: PasswordChangeRequest, authentication: Authentication): ResponseEntity<*> {
        val status = Status()
        status.status = 0
        status.message = ""

        val profileCandidate = profileRepository.findByIdAndDeletedAtIsNull((authentication.principal as UserPrincipal).id)
        if(!profileCandidate.isPresent) {
            status.message = "User not found!"
            return ResponseEntity(status, HttpStatus.OK)
        }

        if (passwordChangeRequest.getConfirmPassword() != passwordChangeRequest.getNewPassword())  {
            status.message = "Passwords are not same"
            return ResponseEntity(status, HttpStatus.OK)
        }

        if (!encoder.matches(passwordChangeRequest.getPassword(), profileCandidate.get().pwd())) {
            status.message = "Current is not true"
            return ResponseEntity(status, HttpStatus.OK)
        }

        profileCandidate.get().setPassword(encoder.encode(passwordChangeRequest.getNewPassword()))
        profileRepository.save(profileCandidate.get())

        status.status = 1
        status.message = "Password changed successfully"
        return ResponseEntity.ok(status)
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    fun logoutUser(value: MutableMap<String, String>, authentication: Authentication): ResponseEntity<*> {
        val profileCandidate = profileRepository.findByUsernameIgnoreCaseAndDeletedAtIsNull(authentication.name!!)
        if(!profileCandidate.isPresent) { return ResponseEntity("User does not exist", HttpStatus.BAD_REQUEST) }
        if (value.containsKey("refresh_token") && !value["refresh_token"].isNullOrBlank()) {
            val refreshTokenCandidate = refreshTokenRepository
                .findByProfileIdAndToken(profileCandidate.get().getId()!!, UUID.fromString(value["refresh_token"]))

            if(!refreshTokenCandidate.isPresent) { return ResponseEntity("Refresh does not exist", HttpStatus.BAD_REQUEST)  }

            refreshTokenRepository.deleteById(refreshTokenCandidate.get().getId()!!)
        }
        if (value.containsKey("device_id") && !value["device_id"].isNullOrBlank()) {
            val deviceMobile = deviceMobileRepository.findByDeviceIdAndDeletedAtIsNull(value["device_id"].toString())
            if (deviceMobile.isPresent) {
                deviceMobileRepository.delete(deviceMobile.get())
            }
        }

        return ResponseEntity("Log out successful", HttpStatus.OK)
    }

    @PostMapping("/register")
    fun registerUser(@Valid @RequestBody registrationRequest: RegistrationRequest): ResponseEntity<*> {
        var user = authService.registerUser(registrationRequest)
        if(!user.isPresent) { throw UserRegistrationException(registrationRequest.getEmail()!!, "Missing user object in database") }

        var urlBuilder: UriComponentsBuilder = ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/auth/registrationconfirmation")
        var onUserRegistrationCompleteEvent = OnUserRegistrationCompleteEvent(user.get(), urlBuilder)
        applicationEventPublisher.publishEvent(onUserRegistrationCompleteEvent)
        logger.info("Registered User returned [API[: $user")
        return ResponseEntity.ok("User registered successfully. Check your email for verification")
    }

    @GetMapping("/registrationconfirmation")
    fun confirmRegistration(@RequestParam("token") token: String): ResponseEntity<*> {
        val user = authService.confirmEmailRegistration(token)
        if(user.isPresent)
            return ResponseEntity("User verified successfully", HttpStatus.OK)
        else
            throw InvalidTokenRequestException("Email Verification Token", token, "Failed to confirm. Please generate a new email verification request")
    }

    @GetMapping("/resendregistrationtoken")
    fun resendRegistrationToken(@RequestParam("token") existingToken: String): ResponseEntity<*>{
        val existedLink = emailVerificationTokenRepository.findByToken(existingToken)
        if(existedLink.isPresent) {
            var leftTimeMs = ((Date.from(existedLink.get().getExpiryDate()).time - 60 * 59 * 1000) - System.currentTimeMillis())
            if (leftTimeMs in 0..60000) {
                return ResponseEntity("You already send verification link for your email, try after ${leftTimeMs / 1000} seconds please!", HttpStatus.BAD_REQUEST)
            }
        }

        var newEmailToken: Optional<EmailVerificationToken> = authService.recreateRegistrationToken(existingToken)
        if(!newEmailToken.isPresent) { throw InvalidTokenRequestException("Email Verification Token", existingToken, "User is already registered. No need to re-generate token") }

        try {
            var regisredUserId = newEmailToken.get().getProfileId()
            val urlBuilder: UriComponentsBuilder = ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/auth/registrationconfirmation")
            var regenerateEmailVerificationEvent: OnRegenerateEmailVerificationEvent = OnRegenerateEmailVerificationEvent(regisredUserId!!, urlBuilder, newEmailToken.get())
            applicationEventPublisher.publishEvent(regenerateEmailVerificationEvent)
            return ResponseEntity.ok("Email verification resent successfully")
        } catch (e: Exception) {
            throw InvalidTokenRequestException("Email Verification Token", existingToken, "No user associated with this request. Re-verification denied")
        }
    }

    @PostMapping("/social/google")
    fun restCall(@Valid @RequestBody userToken: MutableMap<String, Any?>): Any? {
        val idToken: String? = userToken["idToken"] as String?
        println(idToken)
        // idToken comes from the HTTP Header
//        println(FirebaseAuth.getInstance().getUserByEmailAsync("suiebayzh@gmail.com"))
        val decodedToken = FirebaseAuth.getInstance().verifyIdTokenAsync(idToken).get()
        val uid = decodedToken.uid

        // process the code here
        // once it is done
//        return FirebaseAuth.getInstance().getUserByEmail("bekzat.saylaubay@gmail.com")
        return decodedToken
    }

    @PostMapping("/{typeSign}/{providerId}/oauth2")
    fun googleV2(
        @RequestHeader headers: Map<String, String>,
        @PathVariable(value = "typeSign") typeSign: String,
        @PathVariable(value = "providerId") providerId: String,
        @Valid @RequestBody user: User
    ): ResponseEntity<*> {
        println(headers)
        val secret = if (typeSign == "signin-auth") {
            "secret"
        } else {
            "default"
        }
        when (providerId) {
            "google" -> {
                println(providerId)
                return ResponseEntity.ok(authService.googleAuth(user, secret))
            }
        }
        return ResponseEntity.ok("User registered successfully. Check your email for verification")
    }

    @PostMapping("/account/islogging")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    fun getUserIsLoggingAndRole(@Valid @RequestBody roles: List<String>, authentication: Authentication): ResponseEntity<*> {
        val status = Status()
        status.status = 0
        status.message = "Bad response!"
        val user = profileRepository.findByUsernameIgnoreCaseAndDeletedAtIsNull(authentication.name)

        if (
            user.isPresent
            && user.get().getEnabled()!!
            && user.get().getIsBlocked() == null
        ) {
            if (roles.isEmpty()) {
                status.status = 1
                status.message = "Greate response!"
                return ResponseEntity(status, HttpStatus.OK)
            }

            val rolesProfile = rolesGroupService.getRolesByProfileMap(user.get().getId()!!, arrayOf("key"))
            val result = rolesProfile.filter { p -> roles.any { it.lowercase() == p["key"].toString().lowercase() } }
            if (result.isNotEmpty()) {
                status.status = 1
                status.message = "Greate response!"
            }
        }
//        status.value = (authentication.principal as UserPrincipal)

        return ResponseEntity(status, HttpStatus.OK)
    }

    @GetMapping("/user/role/super/admin")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    fun isAdmin(authentication: Authentication): ResponseEntity<*> {
        val status = Status()
        status.message = "Not access!"
        if (
            (authentication.principal is UserPrincipal)
            && profileService.isAdmin((authentication.principal as UserPrincipal).id)
        ) {
            status.status = 1
            status.message = "Have access!!"
        }
        
        return ResponseEntity(status, HttpStatus.OK)
    }

    @GetMapping("/user/role")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    fun getRolesV2(authentication: Authentication): ResponseEntity<*> {
        val status = Status()
        status.status = 0
        status.message = "Have access!"
        val user = profileRepository.findByUsernameIgnoreCaseAndDeletedAtIsNull(authentication.name)
        if (user.isPresent) {
            val rolesProfile = rolesGroupService.getRolesByProfileMap(user.get().getId()!!, arrayOf("key"))
            status.status = 1
            var res: ArrayList<String> = arrayListOf()
            for (item in rolesProfile) {
                res.add(item["key"].toString().lowercase().replace("role_", ""))
            }
            status.value = res
            return ResponseEntity(status, HttpStatus.OK)
        }
        return ResponseEntity(status, HttpStatus.OK)
    }

}
