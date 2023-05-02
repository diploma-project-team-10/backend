package com.mdsp.backend.app.community.analytics.repository

import com.mdsp.backend.app.community.analytics.model.Analytics
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param
import java.util.*

interface IAnalyticsRepository: JpaRepository<com.mdsp.backend.app.community.analytics.model.Analytics, Long> {
    fun findByUserIdAndDeletedAtIsNull(@Param("user_id") user_id: UUID): Optional<com.mdsp.backend.app.community.analytics.model.Analytics>
}
