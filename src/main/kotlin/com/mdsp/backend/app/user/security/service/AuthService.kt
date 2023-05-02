package com.mdsp.backend.app.user.security.service

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.mdsp.backend.app.profile.model.Profile
import com.mdsp.backend.app.profile.repository.IProfileRepository
import com.mdsp.backend.app.system.model.Status
import com.mdsp.backend.app.user.exception.PasswordResetLinkException
import com.mdsp.backend.app.user.exception.ResourceAlreadyInUseException
import com.mdsp.backend.app.user.exception.ResourceNotFoundException
import com.mdsp.backend.app.user.model.PasswordResetToken
import com.mdsp.backend.app.user.model.payload.*
import com.mdsp.backend.app.user.model.token.EmailVerificationToken
import com.mdsp.backend.app.user.model.token.RefreshToken
import com.mdsp.backend.app.user.repository.RefreshTokenRepository
import com.mdsp.backend.app.user.security.jwt.JwtProvider
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.util.*
import kotlin.Any
import kotlin.Exception
import kotlin.Long
import kotlin.RuntimeException
import kotlin.String


@Service
class AuthService {
    private val logger: Logger = LoggerFactory.getLogger(JwtProvider::class.java)

    @Autowired
    lateinit var authenticationManager: AuthenticationManager

    @Autowired
    lateinit var emailVerificationTokenService: EmailVerificationTokenService

    @Autowired
    lateinit var refreshTokenService: RefreshTokenService

    @Autowired
    lateinit var passwordResetTokenService: PasswordResetTokenService

    @Autowired
    lateinit var refreshTokenRepository: RefreshTokenRepository

    @Autowired
    lateinit var profileRepository: IProfileRepository

    @Autowired
    lateinit var tokenProvider: JwtProvider

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    lateinit var encoder: PasswordEncoder

    @Autowired
    lateinit var googleService: GoogleService

    fun authenticateUser(loginRequest: User, withPassword: Boolean = true): Optional<Authentication> {
        if (!withPassword) {
            return Optional.ofNullable(
                UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    null,
                    AuthorityUtils.createAuthorityList()
                )
            )
        }
        return Optional.ofNullable(authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(),
                loginRequest.getPassword()
            )
        ))

    }

    fun generateToken(username: String): String {
        return tokenProvider.generateAccessJwtToken(username)
    }

    fun createAndPersistRefreshToken(loginRequest: User): Optional<RefreshToken> {
        var currentUser = profileRepository.findByUsernameAndDeletedAtIsNull(loginRequest.getUsername()!!)

        var refreshToken: RefreshToken = refreshTokenService.createRefreshToken()
        refreshToken.setProfileId(currentUser.get().getId()!!)
        refreshToken = refreshTokenService.save(refreshToken)
        return Optional.ofNullable<RefreshToken?>(refreshToken)
    }

    fun updateAndPersistRefreshToken(username: String, expiredRefresh: UUID): Optional<RefreshToken> {
        var currentUser = profileRepository.findByUsernameAndDeletedAtIsNull(username)
        var refreshTokenCandidate = refreshTokenRepository.findByProfileIdAndToken(currentUser.get().getId()!!, expiredRefresh)
        if(refreshTokenCandidate.isPresent) { refreshTokenRepository.deleteById(refreshTokenCandidate.get().getId()!!) }

        var refreshToken: RefreshToken = refreshTokenService.createRefreshToken()
        refreshToken.setProfileId(currentUser.get().getId()!!)
        refreshToken.setFromUsedToken(expiredRefresh)
        refreshToken = refreshTokenService.save(refreshToken)
        return Optional.ofNullable<RefreshToken?>(refreshToken)
    }

    fun refreshJwtToken(tokenRefreshRequest: TokenRefreshRequest): Optional<String> {
        val requestRefreshToken: UUID = tokenRefreshRequest.getRefreshToken()!!
        val refreshToken = refreshTokenService.findByToken(requestRefreshToken)
        if (!refreshToken.isPresent) { return Optional.empty() }


        if (refreshTokenService.verifyExpiration(refreshToken.get())) {
            refreshTokenService.increaseCount(refreshToken.get())
        } else {
            return Optional.empty()
        }
        val currentUser = profileRepository.findByIdAndDeletedAtIsNull(refreshToken.get().getProfileId()!!)
        if (!currentUser.isPresent) { return Optional.empty() }
        val newToken = generateToken(currentUser.get().getUsername()!!)

        return Optional.of(newToken)
    }

    fun generatePasswordResetToken(passwordResetLinkRequest: PasswordResetLinkRequest): Optional<PasswordResetToken>{
        var email: String = passwordResetLinkRequest.getEmail()!!
        val profileCandidate = profileRepository.findByEmailAndDeletedAtIsNull(email)
        if(!profileCandidate.isPresent) { throw PasswordResetLinkException(email, "No matching user found for the given request") }

        var passwordResetToken: PasswordResetToken = passwordResetTokenService.createToken()
        passwordResetToken.setUser(profileCandidate.get().getId()!!)
        passwordResetTokenService.save(passwordResetToken)
        return Optional.of(passwordResetToken)
    }

    fun resetPassword(passwordResetRequest: PasswordResetRequest): Optional<UUID> {
        val token: String? = passwordResetRequest.getToken()
        val passwordResetToken: Optional<PasswordResetToken> = passwordResetTokenService.findByToken(UUID.fromString(token!!))
        if(!passwordResetToken.isPresent) { throw ResourceNotFoundException("Password Reset Token", "Token Id", token) }

        passwordResetTokenService.verifyExpiration(passwordResetToken.get())
        val encodedPassword: String = passwordEncoder.encode(passwordResetRequest.getPassword())

        val profileCandidate = profileRepository.findByIdAndDeletedAtIsNull(passwordResetToken.get().getProfileId()!!)
        profileCandidate.get().setPassword(encodedPassword)
        profileCandidate.get().setIsBlocked(null)
        profileCandidate.get().setLoginAttempts(0)
        profileRepository.save(profileCandidate.get())
        return Optional.of(profileCandidate.get().getId()!!)
    }

    fun confirmEmailRegistration(emailToken: String): Optional<UUID> {
        val emailVerificationToken: Optional<EmailVerificationToken> = emailVerificationTokenService.findByToken(emailToken)
        if(!emailVerificationToken.isPresent) { throw ResourceNotFoundException("Token", "Email Verification", emailToken) }

        val registeredUser: UUID = emailVerificationToken.get().getProfileId()!!
        val profileCandidate = profileRepository.findByIdAndDeletedAtIsNull(registeredUser)

        if(profileCandidate.get().getEmailVerified()!!){
            logger.info("User [$emailToken] already registered.")
            return Optional.of(registeredUser)
        }

        emailVerificationTokenService.verifyExpiration(emailVerificationToken.get())
        emailVerificationToken.get().setConfirmedStatus()
        emailVerificationTokenService.save(emailVerificationToken.get())

        profileCandidate.get().markVerificationConfirmed()
        profileRepository.save(profileCandidate.get())
        return Optional.of(registeredUser)
    }

    fun recreateRegistrationToken(existingToken: String): Optional<EmailVerificationToken> {
        var emailVerificationToken: Optional<EmailVerificationToken> = emailVerificationTokenService.findByToken(existingToken)
        if(!emailVerificationToken.isPresent) { throw ResourceNotFoundException("Token", "Existing email verification", existingToken) }

        var profileCandidate = profileRepository.findByIdAndDeletedAtIsNull(emailVerificationToken.get().getProfileId()!!)

        if(profileCandidate.isPresent && profileCandidate.get().getEmailVerified() == true) {
            return Optional.empty()
        }
        return Optional.ofNullable(emailVerificationTokenService.updateExistingTokenWithNameAndExpiry(emailVerificationToken.get()))
    }

    fun registerUser(newRegistrationRequest: RegistrationRequest): Optional<Profile> {
        var newRegistrationRequestEmail = newRegistrationRequest.getEmail()
        var profileCandidate = profileRepository.findByEmailAndDeletedAtIsNull(newRegistrationRequestEmail!!)
        if(profileCandidate.isPresent) {
            logger.error("Email alreay exists: " + newRegistrationRequestEmail)
            throw ResourceAlreadyInUseException("Email", "Address", newRegistrationRequestEmail)
        }
        logger.info("Trying to register new user [$newRegistrationRequest]")
        val newUser = Profile()
        newUser.setFirstName(newRegistrationRequest.firstName)
        newUser.setLastName(newRegistrationRequest.lastName)
        newUser.setEmail(newRegistrationRequest.getEmail())
        newUser.setPassword(encoder.encode(newRegistrationRequest.getPassword()))
        newUser.setUsername(newRegistrationRequest.getEmail())
        newUser.setEmailVerified(true)
        newUser.setEnabled(true)
        profileRepository.save(newUser)
        return Optional.ofNullable(newUser)
    }

    fun authenticate(loginRequest: User, typeRequest: String = "default", withPassword: Boolean = true): Status {
        val status = Status()
        var userCandidate: Optional<Profile> = profileRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(loginRequest.getUsername()!!)
        if (!userCandidate.isPresent) {
            userCandidate = profileRepository.findByUsernameAndDeletedAtIsNull(loginRequest.getUsername()!!)
        } else {
            // Find by Phone Number
        }
        if(!userCandidate.isPresent || userCandidate.get().getEnabled() != true) {
            println(loginRequest.getUsername())
            status.message = "User not found or password wrong!"
            logger.info("User not found or password wrong! User: ${loginRequest.getUsername()}")
            return status
        }
        try {
            if(userCandidate.get().getIsBlocked() != null && userCandidate.get().getIsBlocked()!!.time + 1800000 - System.currentTimeMillis() > 0){
                throw RuntimeException()
            }
            loginRequest.setUsername(userCandidate.get().getUsername())
            val authentication: Optional<Authentication> = authenticateUser(loginRequest, withPassword)
            SecurityContextHolder.getContext().authentication = authentication.get()

            userCandidate.get().setLoginAttempts(0)
            userCandidate.get().setIsBlocked(null)
            profileRepository.save(userCandidate.get())
            val refreshTokenOptional = createAndPersistRefreshToken(loginRequest)
            if (!refreshTokenOptional.isPresent) {
                status.message = "Couldn't create refresh token for: ${loginRequest}"
                logger.info("Couldn't create refresh token for: ${loginRequest.getUsername()}")
                return status
            }
            val refreshToken = refreshTokenOptional.get().getToken()
            val jwtToken: String = generateToken(authentication.get().name)
            status.status = 1
            status.message = "OK"
            if (typeRequest == "secret") {
                val jwtAuth = JwtAuthenticationSecretResponse(jwtToken, refreshToken, tokenProvider.getExpiryDuration())
//                jwtAuth.setLanguage(userCandidate.get().getLanguage())
                status.value = jwtAuth
            } else {
                status.value = JwtAuthenticationResponse(jwtToken, refreshToken, tokenProvider.getExpiryDuration())
            }
            return status
        } catch (e: Exception){
            if(userCandidate.get().getLoginAttempts() >= 5){
                if(userCandidate.get().getIsBlocked() == null) {
                    userCandidate.get().setIsBlocked(Timestamp(System.currentTimeMillis()))
                }
                val blockedTimeMs: Long = userCandidate.get().getIsBlocked()!!.time
                if (blockedTimeMs + 1800000 - System.currentTimeMillis() < 0) {
                    userCandidate.get().setLoginAttempts(1)
                    userCandidate.get().setIsBlocked(null)
                    profileRepository.save(userCandidate.get())
                    status.message = "User not found or password wrong!"
                    logger.info("User not found or password wrong! User: ${loginRequest.getUsername()}")
                    return status
                }

                val leftMinutes: Long = ((blockedTimeMs + 1800000 - System.currentTimeMillis()) / 1000) / 60
                val leftSeconds: Long = ((blockedTimeMs + 1800000 - System.currentTimeMillis()) / 1000) % 60

                profileRepository.save(userCandidate.get())
                status.message = "Your account will unblocked after $leftMinutes minutes and $leftSeconds seconds!"
                return status
            }
            userCandidate.get().incrementLoginAttempts()
            profileRepository.save(userCandidate.get())
            status.message = "User not found or password wrong!"
            logger.info("User not found or password wrong! User: ${loginRequest.getUsername()}")
            return status
        }

    }

    fun googleAuth(user: User, type: String = "default"): Status {
        if (user.getIdToken() == null) {
            return Status()
        }
        val gAuth: GoogleIdToken.Payload? = googleService.getProfileDetailByToken(user.getIdToken()!!)
        if (gAuth != null) {
            val userId = gAuth.subject
            println(gAuth)

            // Get profile information from payload
            val email = gAuth.email
            val emailVerified = gAuth.emailVerified as Boolean

            if (emailVerified && email.isNotEmpty()) {
                user.setUsername(email)
                return authenticate(user, type, false)
            }
        }
        return Status()

    }

}
