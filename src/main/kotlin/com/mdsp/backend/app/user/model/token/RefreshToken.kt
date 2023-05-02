package com.mdsp.backend.app.user.model.token

import com.mdsp.backend.app.profile.model.Profile
import com.mdsp.backend.app.system.model.DateAudit
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.NaturalId
import java.time.Instant
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "REFRESH_TOKEN")
class RefreshToken: DateAudit {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "ID", updatable = false)
    private var id: UUID? = null

    @Column(name = "TOKEN", nullable = false, unique = true)
    private lateinit var token: UUID

    @Column(name = "PROFILE_ID")
    private var profileId: UUID? = null

    @Column(name = "REFRESH_COUNT")
    private var refreshCount: Long? = null

    @Column(name = "EXPIRY_DT", nullable = false)
    private lateinit var  expiryDate: Instant

    @Column(name = "FROM_USED_TOKEN")
    private lateinit var  fromUsedToken: UUID

    constructor() {}

    constructor(id: UUID?, token: UUID, profileId: UUID?, refreshCount: Long?, expiryDate: Instant) {
        this.id = id
        this.token = token
        this.profileId = profileId
        this.refreshCount = refreshCount
        this.expiryDate = expiryDate
    }

    fun incrementRefreshCount() { this.refreshCount = this.refreshCount!! + 1 }

    fun getId() = this.id
    fun setId(id: UUID) { this.id = id }

    fun getToken() = this.token
    fun setToken(token: UUID) { this.token = token }

    fun getProfileId() = profileId
    fun setProfileId(profileId: UUID) { this.profileId = profileId }

    fun getExpiryDate() = this.expiryDate
    fun setExpiryDate(expiryDate: Instant) { this.expiryDate = expiryDate }

    fun getRefreshCount() = this.refreshCount
    fun setRefreshCount(refreshCount: Long?) { this.refreshCount = refreshCount }

    fun getFromUsedToken() = this.fromUsedToken
    fun setFromUsedToken(fromUsedToken: UUID) { this.fromUsedToken = fromUsedToken }
}