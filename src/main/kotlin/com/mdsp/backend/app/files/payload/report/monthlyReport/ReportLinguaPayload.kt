package com.mdsp.backend.app.files.payload.report.monthlyReport

class ReportLinguaPayload {
    var attendance: Number?
    var attendanceAvg: Double?
    var gradesCount: Number?
    var grades: Double?
    var gradesAvg: Double?
    var mocks: Array<Number>?
    var mocksAvg: Double?

    constructor(
        attendance: Number?,
        attendanceAvg: Double?,
        gradesCount: Number?,
        grades: Double?,
        gradesAvg: Double?,
        mocks: Array<Number>?,
        mocksAvg: Double?
    ) {
        this.attendance = attendance
        this.attendanceAvg = attendanceAvg
        this.gradesCount = gradesCount
        this.grades = grades
        this.gradesAvg = gradesAvg
        this.mocks = mocks
        this.mocksAvg = mocksAvg
    }

    override fun toString(): String {
        return "ReportLinguaPayload(attendance=$attendance, attendanceAvg=$attendanceAvg, gradesCount=$gradesCount, grades=$grades, gradesAvg=$gradesAvg, mocks=${mocks?.contentToString()}, mocksAvg=$mocksAvg)"
    }


}
