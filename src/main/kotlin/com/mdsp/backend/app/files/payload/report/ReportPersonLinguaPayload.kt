package com.mdsp.backend.app.files.payload.report

import com.mdsp.backend.app.files.payload.report.monthlyReport.MonthReportPayload
import com.mdsp.backend.app.files.payload.report.monthlyReport.ReportLinguaPayload
import java.util.*

class ReportPersonLinguaPayload {
    var avg: MutableMap<String, Double?>? = null
    var fio: String? = null
    var id: UUID? = null
    var reports: MutableMap<String, ReportLinguaPayload>? = null
    var final: MutableMap<String, Double?>? = null
    var course: Int? = null
    var university: UUID? = null
    var universityString: String? = ""
    var speciality: UUID? = null
    var specialityString: String? = ""

    constructor(
        avg: MutableMap<String, Double?>?,
        fio: String?,
        id: UUID?,
        reports: MutableMap<String, ReportLinguaPayload>?,
        final: MutableMap<String, Double?>?,
        course: Int?,
        university: UUID?,
        speciality: UUID?
    ) {
        this.avg = avg
        this.fio = fio
        this.id = id
        this.reports = reports
        this.final = final
        this.course = course
        this.university = university
        this.speciality = speciality
    }


    override fun toString(): String {
        return "ReportPersonLinguaPayload(avg=$avg, final=$final, fio=$fio, id=$id, reports=$reports)"
    }

}
