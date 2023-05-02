package com.mdsp.backend.app.files.payload.report.monthlyReport

class MonthReportPayload {
    var grades: Double?
    var gradesAvg: Double?
    var gradesCount: Double?

    constructor(grades: Double?, gradesAvg: Double?, gradesCount: Double?) {
        this.grades = grades
        this.gradesAvg = gradesAvg
        this.gradesCount = gradesCount
    }

    override fun toString(): String {
        return "ReportPayload(grades=$grades, gradesAvg=$gradesAvg, gradesCount=$gradesCount)"
    }

}
