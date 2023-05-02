package com.mdsp.backend.app.files.payload.report

import java.util.*

class ReportMentorPayload {
    var fio: String? = null
    var id: UUID? = null
    var course: Int? = null
    var university: UUID? = null
    var universityString: String? = ""
    var speciality: UUID? = null
    var specialityString: String? = ""
//    var course: MutableMap<String, String?>? = null
    var edutest: MutableMap<String, String?>? = null
    var eduway: MutableMap<String, String?>? = null
    var gpa: MutableMap<String, String?>? = null
    var lingua: MutableMap<String, String?>? = null
    var passport: MutableMap<String, String?>? = null
    var practice: MutableMap<String, String?>? = null
    var reading: MutableMap<String, String?>? = null
    var seminar: MutableMap<String, String?>? = null

    constructor(){}
    constructor(
        fio: String?,
        id: UUID?,
        course: Int?,
        university: UUID?,
        speciality: UUID?,
        edutest: MutableMap<String, String?>?,
        eduway: MutableMap<String, String?>?,
        gpa: MutableMap<String, String?>?,
        lingua: MutableMap<String, String?>?,
        passport: MutableMap<String, String?>?,
        practice: MutableMap<String, String?>?,
        reading: MutableMap<String, String?>?,
        seminar: MutableMap<String, String?>?
    ) {
        this.fio = fio
        this.id = id
        this.course = course
        this.university = university
        this.speciality = speciality
        this.edutest = edutest
        this.eduway = eduway
        this.gpa = gpa
        this.lingua = lingua
        this.passport = passport
        this.practice = practice
        this.reading = reading
        this.seminar = seminar
    }

}
