package com.mdsp.backend.app.user;

/***
 *      **********
 *      DashboardController
 *      **********
 *     private fun linguaRating(filter: MutableMap<String, String>? = mutableMapOf()): MutableMap<String, Any?> {
 *         val current = StudentService.getCurrentSemester()
 *         val filter2 = filter ?: mutableMapOf()
 *         if (!filter2.containsKey("st")) {
 *             filter2["st"] = "${current["semester"]}"
 *         }
 *         val sem = filter2["st"]
 *         val report = linguaService.report(filter2)
 *         val result: MutableMap<String, Any?> = mutableMapOf()
 *         for (item in report) {
 *             for (student in (item["students"] as List<Any?>)) {
 *                 val studentMap = student as MutableMap<String, Any?>
 *                 val p = profileRepository.findByIdAndDeletedAtIsNull(UUID.fromString(studentMap["id"].toString()))
 *                 if (p.isPresent) {
 *                     result[p.get().getId().toString()] = mutableMapOf(
 *                         "profileId" to p.get().getId().toString(),
 *                         "fio" to p.get().getFio(),
 *                         "course" to p.get().getCourse(),
 *                         "gender_enum" to p.get().getGenderEnum()[0]["value"],
 *                         "avatar_ref" to null,
 *                         "points" to ((studentMap["avg"] as MutableMap<String, Double?>)["semester$sem"]?:0.0).toLong()
 *                     )
 *                 }
 *             }
 *         }
 *         setMentor(result)
 *
 *         return result
 *     }
 *
 *     private fun readingRating(filter: MutableMap<String, String>? = mutableMapOf()): MutableMap<String, Any?> {
 *         val current = StudentService.getCurrentSemester()
 *         val filter2 = filter ?: mutableMapOf("st" to "$current")
 *         if (!filter2.containsKey("st")) {
 *             filter2["st"] = "${current["semester"]}"
 *         }
 *         val sem = filter2["st"]
 *         val report = readingService.report(filter2)
 *         val result: MutableMap<String, Any?> = mutableMapOf()
 *         for (item in report) {
 *             for (student in (item["students"] as List<Any?>)) {
 *                 val studentMap = student as MutableMap<String, Any?>
 *                 val p = profileRepository.findByIdAndDeletedAtIsNull(UUID.fromString(studentMap["id"].toString()))
 *                 if (p.isPresent) {
 *                     result[p.get().getId().toString()] = mutableMapOf(
 *                         "profileId" to p.get().getId().toString(),
 *                         "fio" to p.get().getFio(),
 *                         "course" to p.get().getCourse(),
 *                         "gender_enum" to p.get().getGenderEnum()[0]["value"],
 *                         "avatar_ref" to null,
 *                         "points" to ((studentMap["avg"] as MutableMap<String, Double?>)["semester$sem"]?:0.0).toLong()
 *                     )
 *                 }
 *             }
 *         }
 *
 *         setMentor(result)
 *
 *         return result
 *     }
 *
 *     private fun passportRating(filter: MutableMap<String, String>? = mutableMapOf()): MutableMap<String, Any?> {
 *         val current = StudentService.getCurrentSemester()
 *         val filter2 = filter ?: mutableMapOf("st" to "$current")
 *         if (!filter2.containsKey("st")) {
 *             filter2["st"] = "${current["semester"]}"
 *         }
 *         val sem = filter2["st"]
 *         val report = passportService.report(filter2)
 *         val result: MutableMap<String, Any?> = mutableMapOf()
 *         for (item in report) {
 *             for (student in (item["students"] as List<Any?>)) {
 *                 val studentMap = student as MutableMap<String, Any?>
 *                 val p = profileRepository.findByIdAndDeletedAtIsNull(UUID.fromString(studentMap["id"].toString()))
 *                 if (p.isPresent) {
 *                     result[p.get().getId().toString()] = mutableMapOf(
 *                         "profileId" to p.get().getId().toString(),
 *                         "fio" to p.get().getFio(),
 *                         "course" to p.get().getCourse(),
 *                         "gender_enum" to p.get().getGenderEnum()[0]["value"],
 *                         "avatar_ref" to null,
 *                         "points" to ((studentMap["avg"] as MutableMap<String, Double?>)["semester$sem"]?:0.0).toLong()
 *                     )
 *                 }
 *             }
 *         }
 *
 *         setMentor(result)
 *         return result
 *     }
 *
 *     private fun eduGradeRating(type: String, filter: MutableMap<String, String>? = mutableMapOf()): MutableMap<String, Any?> {
 *         val current = StudentService.getCurrentSemester()
 *         val filter2 = filter ?: mutableMapOf("st" to "$current")
 *         if (!filter2.containsKey("st")) {
 *             filter2["st"] = "${current["semester"]}"
 *         }
 *         val sem = filter2["st"]
 *         val report = eduGradeService.report(type, filter2)
 *         val result: MutableMap<String, Any?> = mutableMapOf()
 *         for (item in report) {
 *             for (student in (item["students"] as List<Any?>)) {
 *                 val studentMap = student as MutableMap<String, Any?>
 *                 val p = profileRepository.findByIdAndDeletedAtIsNull(UUID.fromString(studentMap["id"].toString()))
 *                 if (p.isPresent) {
 *                     result[p.get().getId().toString()] = mutableMapOf(
 *                         "profileId" to p.get().getId().toString(),
 *                         "fio" to p.get().getFio(),
 *                         "course" to p.get().getCourse(),
 *                         "gender_enum" to p.get().getGenderEnum()[0]["value"],
 *                         "avatar_ref" to null,
 *                         "points" to ((studentMap["avg"] as MutableMap<String, Double?>)["semester$sem"]?:0.0).toLong()
 *                     )
 *                 }
 *             }
 *         }
 *
 *         setMentor(result)
 *         return result
 *     }
 * */

/**
 * **********
 *
 * **********
 *
 * //        return when (type) {
 * //            "reading" -> statisticGradeService.readingAll(my, month)
 * //            "lingua" -> statisticGradeService.linguaAll(my, month)
 * //            "passport" -> statisticGradeService.passportAll(my, month)
 * //            else -> {
 * //                if (!eduGradeService.check(type)) {
 * //                    statisticGradeService.edugradeAll(my, type, month)
 * //                } else {
 * //                    mutableMapOf()
 * //                }
 * //            }
 * //        }
* @GetMapping("/avg/my/all")
 *     @PreAuthorize("isAuthenticated()")
 *     fun getMyAvgAll(
 *         authentication: Authentication
 *     ): MutableMap<String, ArrayList<EduAvgPayload>> {
 *         val my = (authentication.principal as UserPrincipal).id
 *         return statisticGradeService.getAvgProfileAll(my)
 *     }
 *
 *     @GetMapping("/avg/{profileId}/all")
 *     @PreAuthorize("hasRole('ADMIN')")
 *     fun getProfileAvgAll(
 *         @PathVariable(value = "profileId") profileId: UUID,
 *         authentication: Authentication
 *     ): MutableMap<String, ArrayList<EduAvgPayload>> {
 *         return statisticGradeService.getAvgProfileAll(profileId)
* */

/***
 *      **********
 *      StatisticGradeService
 *      **********
 *      //        result["reading"] = readingAll(profileId, month)
 * //        result["lingua"] = linguaAll(profileId, month)
 * //        result["passport"] = passportAll(profileId, month)
 *
 * //        for ((key, value) in result) {
 * //            for ((avgKey, avgValue) in avgRating) {
 * //                if ((value as MutableMap<String, Any>).containsKey("$avgKey$str"))
 * //                    avgRating[avgKey] = avgRating[avgKey]!! + (value["$avgKey$str"] as Long)
 * //            }
 * //        }
 * // Mapped
 *     fun passportAll(profileId: UUID, month: Boolean): MutableMap<String, Any?> {
 *         val current: MutableMap<String, LocalDateTime> = StudentService.getCurrentYearStudy()
 *         val avgRating = mutableMapOf("year$str" to 0L, "semester1$str" to 0L, "semester2$str" to 0L)
 *         val passportMap = mutableMapOf<String, Any?>()
 *         for (passportType in semYear) {
 *             val newAvg = EduAvgPayload()
 *             newAvg.title = passportType
 *             newAvg.type = "passport"
 *             newAvg.date = when (passportType) {
 *                 "year" -> current
 *                 "semester1" -> mutableMapOf("startDate" to current["startDate"]!!, "endDate" to current["middleDate"]!!)
 *                 "semester2" -> mutableMapOf("startDate" to current["middleDate"]!!, "endDate" to current["endDate"]!!)
 *                 else -> mutableMapOf()
 *             }
 *             val array = studentTaskRepository.findAllByStudentIdAndStatusAndDeletedAtIsNull(profileId, 2, newAvg.date!!["startDate"]!!, newAvg.date!!["endDate"]!!)
 *             var avg: Long? = null
 *             for (grade in array) {
 *                 avg = (avg?:0L) + (grade.grade * 0.1).toLong()
 *             }
 *             newAvg.avg = avg
 *             avgRating["$passportType$str"] = avg?:0L
 *             passportMap[passportType] = newAvg
 *         }
 *         if (month) {
 *             val passportMonth = getMonth("passport")
 *             for (pm in passportMonth) {
 *                 val avg = studentTaskRepository.avgOfStudent(profileId, 2, pm.date!!["startDate"]!!, (pm.date!!["startDate"]!!.plusMonths(1).minusDays(1)))
 *                 pm.avg = (avg ?: 0.0).toLong()
 *                 pm.isVerified = avg != null
 *             }
 *             for (l in passportMonth) {
 *                 passportMap[l.title!!.lowercase()] = l
 *             }
 *         }
 *         passportMap.putAll(avgRating)
 *         return passportMap
 *     }
 *
 *     // Mapped
 *     fun readingAll(profileId: UUID, month: Boolean = false): MutableMap<String, Any?> {
 *         val current: MutableMap<String, LocalDateTime> = StudentService.getCurrentYearStudy()
 *         val avgRating = mutableMapOf("year$str" to 0L, "semester1$str" to 0L, "semester2$str" to 0L)
 *         val reading = readingService.reportPage(profileId)
 *         val readMap = mutableMapOf<String, Any?>()
 *         for (readType in semYear) {
 *             val newAvg = EduAvgPayload()
 *             newAvg.title = readType
 *             newAvg.type = "reading"
 *             newAvg.date = when (readType) {
 *                 "year" -> current
 *                 "semester1" -> mutableMapOf("startDate" to current["startDate"]!!, "endDate" to current["middleDate"]!!)
 *                 "semester2" -> mutableMapOf("startDate" to current["middleDate"]!!, "endDate" to current["endDate"]!!)
 *                 else -> mutableMapOf()
 *             }
 *             newAvg.avg = (((reading["avg"] as MutableMap<String, Any?>)[readType]?:0.0) as Double).toLong()
 *             avgRating["$readType$str"] = newAvg.avg?:0L
 *             readMap[readType] = newAvg
 *         }
 *
 *         if (month) {
 *             val readingMonth = getMonth("reading")
 *             for (l in readingMonth) {
 *                 readMap[l.title!!.lowercase()] = l
 *             }
 *             for ((key, value) in (reading["reports"] as MutableMap<Int, MutableMap<String, Any?>>)) {
 *                 val monthName = Month.of(key)
 *                 if (readMap.containsKey(monthName.name.lowercase())) {
 *                     (readMap[monthName.name.lowercase()] as EduAvgPayload).avg = (value["gradesAvg"] as Double).toLong()
 *                 }
 *             }
 *         }
 *         readMap.putAll(avgRating)
 *         return readMap
 *     }
 *
 *     // Mapped
 *     fun linguaAll(profileId: UUID, month: Boolean = false): MutableMap<String, Any?> {
 *         val current: MutableMap<String, LocalDateTime> = StudentService.getCurrentYearStudy()
 *         val avgRating = mutableMapOf("year$str" to 0L, "semester1$str" to 0L, "semester2$str" to 0L)
 *         val lingua = linguaService.reportPage(profileId)
 *         val linguaMap = mutableMapOf<String, Any?>()
 *         for (linguaType in semYear) {
 *             val newAvg = EduAvgPayload()
 *             newAvg.title = linguaType
 *             newAvg.type = "lingua"
 *             newAvg.date = when (linguaType) {
 *                 "year" -> current
 *                 "semester1" -> mutableMapOf("startDate" to current["startDate"]!!, "endDate" to current["middleDate"]!!)
 *                 "semester2" -> mutableMapOf("startDate" to current["middleDate"]!!, "endDate" to current["endDate"]!!)
 *                 else -> mutableMapOf()
 *             }
 *             newAvg.avg = (((lingua["avg"] as MutableMap<String, Any?>)[linguaType]?:0.0) as Double).toLong()
 *             avgRating["$linguaType$str"] = newAvg.avg?:0L
 *             linguaMap[linguaType] = newAvg
 *         }
 *         if (month) {
 *             val eduMonth = getMonth("edugrade")
 *             for (l in eduMonth) {
 *                 linguaMap[l.title!!.lowercase()] = l
 *             }
 *             for ((key, value) in (lingua["reports"] as MutableMap<Int, MutableMap<String, Any?>>)) {
 *                 val monthName = Month.of(key)
 *                 if (linguaMap.containsKey(monthName.name.lowercase())) {
 *                     (linguaMap[monthName.name.lowercase()] as EduAvgPayload).avg = (value["gradesAvg"] as Double).toLong()
 *                 }
 *             }
 *         }
 *         linguaMap.putAll(avgRating)
 *         return linguaMap
 *     }
 *
 *     // Mapped
 *     fun edugradeAll(profileId: UUID, type: String, month: Boolean = false): MutableMap<String, Any?> {
 *         val eduMap = mutableMapOf<String, Any?>()
 *         run eduMapping@{
 *             if (types.contains(type)) {
 *                 val profile = profileRepository.findByIdAndDeletedAtIsNull(profileId)
 *                 if (profile.isPresent) {
 *                     when (type) {
 *                         "eduway", "edutest" -> {
 *                             if (profile.get().getCourse() == 3 || profile.get().getCourse() == 4) return@eduMapping
 *                         }
 *                         "practice" -> {
 *                             if (profile.get().getCourse() == 1 || profile.get().getCourse() == 2) return@eduMapping
 *                         }
 *                     }
 *                 } else {
 *                     return@eduMapping
 *                 }
 *                 val current: MutableMap<String, LocalDateTime> = StudentService.getCurrentYearStudy()
 *                 val avgRating = mutableMapOf("year$str" to 0L, "semester1$str" to 0L, "semester2$str" to 0L)
 *                 val edugrade = eduGradeService.reportPage(profileId, type)
 *                 for (eduType in semYear) {
 *                     val newAvg = EduAvgPayload()
 *                     newAvg.title = eduType
 *                     newAvg.type = "edugrade"
 *                     newAvg.date = when (eduType) {
 *                         "year" -> current
 *                         "semester1" -> mutableMapOf("startDate" to current["startDate"]!!, "endDate" to current["middleDate"]!!)
 *                         "semester2" -> mutableMapOf("startDate" to current["middleDate"]!!, "endDate" to current["endDate"]!!)
 *                         else -> mutableMapOf()
 *                     }
 *                     newAvg.avg = (((edugrade["avg"] as MutableMap<String, Any?>)[eduType]?:0.0) as Double).toLong()
 *                     avgRating["$eduType$str"] = newAvg.avg?:0L
 *                     eduMap[eduType] = newAvg
 *                 }
 *
 *                 if (month) {
 *                     val eduMonth = getMonth("edugrade")
 *                     for (l in eduMonth) {
 *                         eduMap[l.title!!.lowercase()] = l
 *                     }
 *                     for ((key, value) in (edugrade["reports"] as MutableMap<Int, MutableMap<String, Any?>>)) {
 *                         val monthName = Month.of(key)
 *                         if (eduMap.containsKey(monthName.name.lowercase())) {
 *                             (eduMap[monthName.name.lowercase()] as EduAvgPayload).avg = (value["gradesAvg"] as Double).toLong()
 *                         }
 *                     }
 *                 }
 *                 eduMap.putAll(avgRating)
 *                 return eduMap
 *             }
 *         }
 *         return eduMap
 *     }
 *
 *     //Parse to ARRAY
 *     fun readingParse(profileId: UUID): ArrayList<EduAvgPayload> {
 *         val current: MutableMap<String, LocalDateTime> = StudentService.getCurrentYearStudy()
 *         val result: ArrayList<EduAvgPayload> = arrayListOf()
 *         val readingType = arrayListOf("year", "1", "2")
 *         val reading = mutableMapOf(
 *             readingType[0] to readingService.statisticProfile(profileId, readingType[0]),
 *             readingType[1] to readingService.statisticProfile(profileId, readingType[1]),
 *             readingType[2] to readingService.statisticProfile(profileId, readingType[2])
 *         )
 *
 *         for (readType in reading) {
 *             val newAvg = EduAvgPayload()
 *             newAvg.title = if (readType.key == "1" || readType.key == "2") "semester" + readType.key else readType.key
 *             newAvg.type = "reading"
 *             newAvg.date = when (readType.key) {
 *                 "year" -> current
 *                 "1" -> mutableMapOf("startDate" to current["startDate"]!!, "endDate" to current["middleDate"]!!)
 *                 "2" -> mutableMapOf("startDate" to current["middleDate"]!!, "endDate" to current["endDate"]!!)
 *                 else -> mutableMapOf()
 *             }
 *             if (readType.key == "1" || readType.key == "2") "semester" + readType.key else readType.key
 *             newAvg.avg = 0
 *             for (avg in readType.value) {
 *                 if (avg["title"] == "average" && avg["average"] != null) {
 *                     newAvg.avg = (avg["average"] as Int).toLong()
 *                 }
 *             }
 *             result.add(newAvg)
 *         }
 *         return result
 *     }
 *
 *     //Previous Version
 *     fun getAvgProfileAll(profileId: UUID): MutableMap<String, ArrayList<EduAvgPayload>> {
 *         val result = mutableMapOf<String, ArrayList<EduAvgPayload>>()
 *         //Edugrade
 *         for (type in types) {
 *             if (type != "gpa") {
 *                 val avgList = eduGradeService.getAvgProfileYear(profileId, type)
 *                 avgList.addAll(eduGradeService.getAvgProfileSemester(profileId, type))
 *                 result[type] = arrayListOf()
 *                 for (avg in avgList) {
 *                     result[type]!!.add(avg)
 *                 }
 *             }
 *         }
 *         //Reading and Lingua
 *         result["reading"] = readingParse(profileId)
 *
 *         val linguaSemYear: ArrayList<EduAvgPayload> = arrayListOf()
 *         val lingua = linguaService.statisticProfile(profileId)
 *         for (group in lingua) {
 *             val newAvg = EduAvgPayload()
 *             newAvg.id = group["id"] as UUID?
 *             newAvg.title = group["title"] as String?
 *             newAvg.type = "lingua"
 *             newAvg.date = mutableMapOf("startDate" to (group["startDate"] as LocalDateTime))
 *             newAvg.avg = group["avg"] as Long
 *             linguaSemYear.add(newAvg)
 *         }
 *         result["lingua"] = linguaSemYear
 *         return result
 *     }
 * */


/***
 *      **********
        StatisticGradeService
        **********

 //for dashboard statistics
 fun getAvgProfilePageMonth(profileId: UUID): MutableMap<String, Any?> {
 val result = mutableMapOf<String, Any?>()
 //Month
 //Edugrade
 for (type in types) {
 //            if (type != "gpa") {
 val list = eduGradeService.getAvgProfileMonth(profileId, type)
 val map = mutableMapOf<String, EduAvgPayload>()
 for (l in list) {
 map[l.title!!.lowercase()] = l
 }
 result[type] = map
 //            }
 }

 //Reading
 val readingMonth = getMonth("reading")
 val reading = readingService.statisticProfile(profileId, "year")
 for (element in reading) {
 if (element["title"] != "average") {
 for (month in readingMonth) {
 if ((element["startDate"]!! as LocalDateTime).plusHours(6).month == month.date!!["startDate"]!!.month) {
 month.avg = (element["average"] as Int?)?.toLong() ?: 0
 month.isVerified = element["verified"].toString().toBoolean()
 }
 }
 }
 }
 val readingMap = mutableMapOf<String, EduAvgPayload>()
 for (l in readingMonth) {
 readingMap[l.title!!.lowercase()] = l
 }
 result["reading"] = readingMap

 //Lingua
 val linguaMap = mutableMapOf<String, EduAvgPayload>()
 val linguaMonth = getMonth("lingua")
 val lingua = linguaService.statisticProfile(profileId)
 for (element in lingua) {
 for (report in element["reports"]!! as ArrayList<MutableMap<String, Any?>>) {
 for (month in linguaMonth) {
 if ((report["startDate"]!! as LocalDateTime).month == month.date!!["startDate"]!!.month) {
 month.avg = ((report["mock"] as Double) * 0.5 + (report["attendance"] as Int) * 0.5).toLong()
 month.isVerified = element["isVerified"].toString().toBoolean()
 }
 }
 }
 }
 for (l in linguaMonth) {
 linguaMap[l.title!!.lowercase()] = l
 }
 result["lingua"] = linguaMap

 return result
 }

//    fun getAvgProfilePage(profileId: UUID, month: Boolean = false): MutableMap<String, Any?> {
////        var result: MutableMap<String, Any?> = mutableMapOf()
//        val current: MutableMap<String, LocalDateTime> = StudentService.getCurrentYearStudy()
//        val result = mutableMapOf<String, Any?>()
//        val avgRating = mutableMapOf<String, Long>("year" to 0L, "semester1" to 0L, "semester2" to 0L)
//
//        //EduGrade
//        for (type in types) {
//            if (type != "gpa") {
//                val avgList = eduGradeService.getAvgProfileSemester(profileId, type)
//                avgList.addAll(eduGradeService.getAvgProfileYear(profileId, type))
//                val map = mutableMapOf<String, EduAvgPayload>()
//                for (avg in avgList) {
//                    if (avgRating.containsKey(avg.title)){
//                        var average: Long = avgRating[avg.title!!]!!
//                        average += avg.avg?: 0
//                        avgRating[avg.title!!] = average
//                        map[avg.title!!.lowercase()] = avg
//                    }
//                }
//                result[type] = map
//            }
//        }
//        if(month) {
//            for (type in types) {
//                if (type != "gpa") {
//                    val list = eduGradeService.getAvgProfileMonth(profileId, type)
//                    val map = mutableMapOf<String, EduAvgPayload>()
//                    for (l in list) {
//                        map[l.title!!.lowercase()] = l
//                    }
//                    (result[type] as MutableMap<String, EduAvgPayload>).putAll(map)
//                }
//            }
//        }
//
//
//        //Reading
//        val reading = readingSemYear(profileId)
//        val readMap = mutableMapOf<String, EduAvgPayload>()
//        for (read in reading) {
//            if (avgRating.containsKey(read.title)){
//                var average: Long = avgRating[read.title!!]!!
//                average += read.avg?: 0
//                avgRating[read.title!!] = average
//                readMap[read.title!!.lowercase()] = read
//            }
//        }
////        result["reading"] = readMap
//
//        if(month) {
//            val readingMonth = getMonth("reading")
//            val readingYear = readingService.statisticProfile(profileId, "year")
//            for (element in readingYear) {
//                if (element["title"] != "average") {
//                    for (month in readingMonth) {
//                        if ((element["startDate"]!! as LocalDateTime).plusHours(6).month == month.date!!["startDate"]!!.month) {
//                            month.avg = (element["average"] as Int?)?.toLong() ?: 0
//                        }
//                    }
//                }
//            }
////        val readingMap = mutableMapOf<String, EduAvgPayload>()
//            for (l in readingMonth) {
//                readMap[l.title!!.lowercase()] = l
//            }
//        }
//        result["reading"] = readingAll(profileId, month)
//
//
//        //Lingua
////        val linguaSemYear: ArrayList<EduAvgPayload> = arrayListOf()
////        val lingua = linguaService.statisticProfile(profileId)
////        for (group in lingua) {
////            val newAvg = EduAvgPayload()
////            newAvg.id = group["id"] as UUID?
////            newAvg.title = group["title"] as String?
////            newAvg.type = "lingua"
////            newAvg.date = mutableMapOf("startDate" to (group["startDate"] as LocalDateTime))
////            newAvg.avg = group["avg"] as Long
////            linguaSemYear.add(newAvg)
////        }
////        val linguaMap = mutableMapOf<String, EduAvgPayload>()
////        for (group in linguaSemYear) {
////            val avg = group.avg!!
////            if (group.date!!["startDate"]!!.year == current["startDate"]!!.year){
////                var average1: Long = avgRating["semester1"]!!
////                average1 += avg
////                avgRating["semester1"] = average1
////                linguaMap["semester1"] = group
//////                semester1Avg += group["avg"] as Long
////            }
////            if (group.date!!["startDate"]!!.year == current["endDate"]!!.year){
////                var average2: Long = avgRating["semester2"]!!
////                average2 += avg
////                avgRating["semester2"] = average2
//////                semester2Avg += group["avg"] as Long
////                linguaMap["semester2"] = group
////            }
////            var average: Long = avgRating["year"]!!
////            average += avg
////            avgRating["year"] = average
////        }
////        for ((key, value) in avgRating) {
////            if(!linguaMap.containsKey(key)) {
////                linguaMap[key] = EduAvgPayload()
////            }
////        }
//////        result["lingua"] = linguaMap
////        if(month) {
////            val linguaMonth = getMonth("lingua")
////            for (element in lingua) {
////                for (report in element["reports"]!! as ArrayList<MutableMap<String, Any?>>) {
////                    for (month in linguaMonth) {
////                        if ((report["startDate"]!! as LocalDateTime).month == month.date!!["startDate"]!!.month) {
////                            month.avg =
////                                ((report["mock"] as Double) * 0.5 + (report["attendance"] as Int) * 0.5).toLong()
////                        }
////                    }
////                }
////            }
////
//////        val linguaMap = mutableMapOf<String, EduAvgPayload>()
////            for (l in linguaMonth) {
////                linguaMap[l.title!!.lowercase()] = l
////            }
////        }
//        result["lingua"] = linguaAll(profileId, month)
//
//        result.putAll(avgRating)
//
////        val monthAvg = getAvgProfilePageMonth(profileId)
////        result.putAll(getAvgProfilePageSemester(profileId))
////        if (month) {
////            for ((key, value) in monthAvg) {
////                if (result.containsKey(key)) {
////                    (result[key] as MutableMap<String, Any?>).putAll(monthAvg[key] as MutableMap<String, Any?>)
////                } else {
////                    result[key] = value
////                }
////            }
////        }
//        return result
//    }
//    fun getAvgProfilePageSemester(profileId: UUID) : MutableMap<String, Any?>{
//        val current: MutableMap<String, LocalDateTime> = StudentService.getCurrentYearStudy()
//        val result = mutableMapOf<String, Any?>()
//        val avgRating = mutableMapOf<String, Long>("year" to 0L, "semester1" to 0L, "semester2" to 0L)
//
//        for (type in types) {
//            if (type != "gpa") {
//                val avgList = eduGradeService.getAvgProfileSemester(profileId, type)
//                avgList.addAll(eduGradeService.getAvgProfileYear(profileId, type))
//                val map = mutableMapOf<String, EduAvgPayload>()
//                for (avg in avgList) {
//                    if (avgRating.containsKey(avg.title)){
//                        var average: Long = avgRating[avg.title!!]!!
//                        average += avg.avg?: 0
//                        avgRating[avg.title!!] = average
//                        map[avg.title!!.lowercase()] = avg
//                    }
//                }
//                result[type] = map
//            }
//        }
//
//        val reading = readingSemYear(profileId)
//        val readMap = mutableMapOf<String, EduAvgPayload>()
//        for (read in reading) {
//            if (avgRating.containsKey(read.title)){
//                var average: Long = avgRating[read.title!!]!!
//                average += read.avg?: 0
//                avgRating[read.title!!] = average
//                readMap[read.title!!.lowercase()] = read
//            }
//        }
//        result["reading"] = readMap
//
//        val lingua = linguaSemYear(profileId)
//        val linguaMap = mutableMapOf<String, EduAvgPayload>()
//        for (group in lingua) {
//            val avg = group.avg!!
//            if (group.date!!["startDate"]!!.year == current["startDate"]!!.year){
//                var average1: Long = avgRating["semester1"]!!
//                average1 += avg
//                avgRating["semester1"] = average1
//                linguaMap["semester1"] = group
////                semester1Avg += group["avg"] as Long
//            }
//            if (group.date!!["startDate"]!!.year == current["endDate"]!!.year){
//                var average2: Long = avgRating["semester2"]!!
//                average2 += avg
//                avgRating["semester2"] = average2
////                semester2Avg += group["avg"] as Long
//                linguaMap["semester2"] = group
//            }
//            var average: Long = avgRating["year"]!!
//            average += avg
//            avgRating["year"] = average
//        }
//        for ((key, value) in avgRating) {
//            if(!linguaMap.containsKey(key)) {
//                linguaMap[key] = EduAvgPayload()
//            }
//        }
//        result["lingua"] = linguaMap
//
//        result.putAll(avgRating)
//        return result
//    }
//    private fun linguaSemYear(profileId: UUID): ArrayList<EduAvgPayload> {
//        val current: MutableMap<String, LocalDateTime> = StudentService.getCurrentYearStudy()
//
//        val result: ArrayList<EduAvgPayload> = arrayListOf()
//        val lingua = linguaService.statisticProfile(profileId)
//        for (group in lingua) {
//            val newAvg = EduAvgPayload()
//            newAvg.id = group["id"] as UUID?
//            newAvg.title = group["title"] as String?
//            newAvg.type = "lingua"
//            newAvg.date = mutableMapOf("startDate" to (group["startDate"] as LocalDateTime))
//            newAvg.avg = group["avg"] as Long
//            result.add(newAvg)
//        }
//        return result
//    }
 ***/
