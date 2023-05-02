package com.mdsp.backend.app.user.model.token

import com.mdsp.backend.app.system.model.DateAudit
import com.mdsp.backend.app.user.model.TokenStatus
import org.hibernate.annotations.GenericGenerator
import java.time.Instant
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "EMAIL_VERIFICATION_TOKEN")
class EmailVerificationToken: DateAudit {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false)
    private var id: UUID? = null

    @Column(name = "TOKEN", unique = true)
    private var token: String? = null

    @Column(name = "PROFILE_ID")
    private var profileId: UUID? = null

    @Column(name = "TOKEN_STATUS")
    @Enumerated(EnumType.STRING)
    private var tokenStatus: TokenStatus? = null

    @Column(name = "EXPIRY_DT")
    private var expiryDate: Instant? = null

    constructor() {}

    constructor(id: UUID?, token: String?, profileId: UUID, tokenStatus: TokenStatus?, expiryDate: Instant?) {
        this.id = id
        this.token = token
        this.profileId = profileId
        this.tokenStatus = tokenStatus
        this.expiryDate = expiryDate
    }

    fun setConfirmedStatus() { setTokenStatus(TokenStatus.STATUS_CONFIRMED) }

    fun getId() = id
    fun setId(id: UUID?) { this.id = id }

    fun getToken() = this.token
    fun setToken(token: String?) { this.token = token }

    fun getProfileId() = this.profileId
    fun setProfileId(profileId: UUID) { this.profileId = profileId }

    fun getExpiryDate() = expiryDate
    fun setExpiryDate(expiryDate: Instant?) { this.expiryDate = expiryDate }

    fun getTokenStatus() = tokenStatus
    fun setTokenStatus(tokenStatus: TokenStatus?) { this.tokenStatus = tokenStatus }
}