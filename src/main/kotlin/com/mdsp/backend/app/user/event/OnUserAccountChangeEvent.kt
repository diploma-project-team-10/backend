package com.mdsp.backend.app.user.event

import org.springframework.context.ApplicationEvent
import java.util.*

class OnUserAccountChangeEvent: ApplicationEvent {

    private var profileId: UUID? = null
    private var action: String? = null
    private var actionStatus: String? = null

    constructor(profileId: UUID?, action: String?, actionStatus: String?): super(profileId!!){
        this.profileId = profileId
        this.action = action
        this.actionStatus = actionStatus
    }

    fun getProfileId() = profileId
    fun setUser(profileId: UUID) { this.profileId = profileId }

    fun getAction() = action
    fun setAction(action: String?) { this.action = action }

    fun getActionStatus() = actionStatus
    fun setActionStatus(actionStatus: String?) { this.actionStatus = actionStatus }
}