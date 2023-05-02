package com.mdsp.backend.app.course.model

import com.mdsp.backend.app.system.model.DateAudit
import com.vladmihalcea.hibernate.type.array.StringArrayType
import org.hibernate.annotations.TypeDef
import org.hibernate.annotations.TypeDefs
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.util.ArrayList
import javax.persistence.*

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
@TypeDefs(
    TypeDef(
        name = "string-array",
        typeClass = StringArrayType::class
    )
)
abstract class StatusAudit : DateAudit() {
    @Column(name = "status")
    @Enumerated(EnumType.ORDINAL)
    private var status: StatusCourse = StatusCourse.DRAFT

    open fun getStatus(): ArrayList<MutableMap<String, Any?>> {
        var result = ""
        when (this.status) {
            StatusCourse.PUBLISHED -> {
                result = "1"
            }
            StatusCourse.PENDING_REVIEW -> {
                result = "2"
            }
            StatusCourse.DRAFT -> {
                result = "3"
            }
            else -> {}
        }
        if (result.isNotEmpty()) {
            return arrayListOf(
                mutableMapOf(
                    "id" to result,
                    "value" to ""
                )
            )
        }
        return arrayListOf()

    }
    open fun setStatus(status: ArrayList<MutableMap<String, Any?>>) {
        this.status = StatusCourse.NONE
        if (status.isNotEmpty()) {
            when (status[0]["id"]) {
                "1" -> {
                    this.status = StatusCourse.PUBLISHED
                }
                "2" -> {
                    this.status = StatusCourse.PENDING_REVIEW
                }
                "3" -> {
                    this.status = StatusCourse.DRAFT
                }
                else -> {
                    this.status = StatusCourse.NONE
                }
            }
        }
    }

}
