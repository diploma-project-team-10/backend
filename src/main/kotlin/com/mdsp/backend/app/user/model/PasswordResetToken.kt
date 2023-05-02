package com.mdsp.backend.app.user.model

import com.mdsp.backend.app.system.model.DateAudit
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.NaturalId
import java.time.Instant
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "PASSWORD_RESET_TOKEN")
class PasswordResetToken: DateAudit {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false)
    private var id: UUID? = null

    @NaturalId
    @Column(name = "TOKEN_NAME", nullable = false, unique = true)
    private var token: UUID? = null

    @Column(name = "EXPIRY_DT", nullable = false)
    private var expiryDate: Instant? = null

    @Column(name = "PROFILE_ID", nullable = false)
    private var profileId: UUID? = null

    constructor() {}

    constructor(id: UUID, token: UUID, expiryDate: Instant, profileId: UUID) {
        this.id = id;
        this.token = token;
        this.expiryDate = expiryDate;
        this.profileId = profileId;
    }

    fun getExpiryDate() = expiryDate
    fun setExpiryDate(expiryDate: Instant?) { this.expiryDate = expiryDate }

    fun getProfileId() = profileId
    fun setUser(profileId: UUID) { this.profileId = profileId }

    fun getToken() = token
    fun setToken(token: UUID?) { this.token = token }
}