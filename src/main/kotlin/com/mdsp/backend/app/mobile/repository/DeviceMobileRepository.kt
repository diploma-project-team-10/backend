package com.mdsp.backend.app.mobile.repository

import com.mdsp.backend.app.mobile.model.DeviceMobile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*
import kotlin.collections.ArrayList

interface DeviceMobileRepository: JpaRepository<DeviceMobile, Long> {
    fun findByIdAndDeletedAtIsNull(id: UUID): Optional<DeviceMobile>

    fun findAllByDeletedAtIsNull(): ArrayList<DeviceMobile>

    fun findByProfileIdAndDeletedAtIsNull(profileId: UUID): ArrayList<DeviceMobile>

    fun findAllByProfileIdInAndDeletedAtIsNull(profileId: List<UUID>): ArrayList<DeviceMobile>

    fun findByDeviceIdAndDeletedAtIsNull(deviceId: String): Optional<DeviceMobile>

    fun findById(@Param("id") id: UUID): Optional<DeviceMobile>

    @Query(
        value = "SELECT device_id FROM device_mobile u WHERE u.deleted_at IS NULL " +
                "AND u.profile_id IN :profileIds",
        nativeQuery = true
    )
    fun getDeviceIds(@Param("profileIds") profileIds: List<UUID>): List<String>
}
