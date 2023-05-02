package com.mdsp.backend.app.mobile.controller

import com.mdsp.backend.app.mobile.model.DeviceMobile
import com.mdsp.backend.app.mobile.repository.DeviceMobileRepository
import com.mdsp.backend.app.profile.repository.*
import com.mdsp.backend.app.profile.service.ProfileService
import com.mdsp.backend.app.reference.repository.IReferenceRepository
import com.mdsp.backend.app.reference.service.RecordNoteService
import com.mdsp.backend.app.structure.service.RolesGroupService
import com.mdsp.backend.app.system.config.DataSourceConfiguration
import com.mdsp.backend.app.system.model.Status
import com.mdsp.backend.app.user.model.UserPrincipal
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.Valid


@RestController
@RequestMapping("/api/mobile/device-id")
class DeviceMobileController {

    @Autowired
    lateinit var profileRepository: IProfileRepository

    @Autowired
    lateinit var referenceRepository: IReferenceRepository

    @Autowired
    lateinit var recordNoteService: RecordNoteService

    @Autowired
    lateinit var rolesGroupService: RolesGroupService

    @Autowired
    lateinit var profileService: ProfileService

    @Autowired
    lateinit var dataSourceConfig: DataSourceConfiguration

    @Autowired
    lateinit var deviceMobileRepository: DeviceMobileRepository

    private val PROFILE_REF: UUID = UUID.fromString("00000000-0000-0000-0000-000000000017")

    @PostMapping("/save")
    @PreAuthorize("isAuthenticated()")
    fun editMyUser(
        @Valid @RequestBody deviceId: String,
        authentication: Authentication
    ): ResponseEntity<*> {
        val status = Status()
        val profileId = (authentication.principal as UserPrincipal).id
        if (deviceId.isNotEmpty()) {
            val deviceMobile = deviceMobileRepository.findByDeviceIdAndDeletedAtIsNull(deviceId)
            if (deviceMobile.isPresent) {
                deviceMobile.get().profileId = profileId
                status.status = 1
                deviceMobileRepository.save(deviceMobile.get())
            } else {
                val newDeviceMobile = DeviceMobile()
                newDeviceMobile.deviceId = deviceId
                newDeviceMobile.profileId = profileId
                status.status = 1
                deviceMobileRepository.save(newDeviceMobile)
            }

        }

        return ResponseEntity(status, HttpStatus.OK)
    }

}
