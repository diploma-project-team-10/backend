package com.mdsp.backend.app.profile.controller

import com.mdsp.backend.app.profile.model.*
import com.mdsp.backend.app.profile.repository.*
import com.mdsp.backend.app.profile.service.ProfileService
import com.mdsp.backend.app.reference.repository.IReferenceRepository
import com.mdsp.backend.app.reference.service.RecordNoteService
import com.mdsp.backend.app.structure.service.RolesGroupService
import com.mdsp.backend.app.system.config.DataSourceConfiguration
import org.apache.commons.lang.StringEscapeUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.*


@RestController
@RequestMapping("/api/profile/fields")
class SpecialityController {

    @Autowired
    lateinit var profileRepository: IProfileRepository

    @Autowired
    lateinit var referenceRepository: IReferenceRepository

    @Autowired
    lateinit var rolesGroupService: RolesGroupService

    @Autowired
    lateinit var profileService: ProfileService

    @Autowired
    lateinit var dataSourceConfig: DataSourceConfiguration

    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    fun getUniverSpec(authentication: Authentication): ResponseEntity<*> {
        val speciality: ArrayList<MutableMap<String, Any?>> = arrayListOf()
        val university: ArrayList<MutableMap<String, Any?>> = arrayListOf()
        val city: ArrayList<MutableMap<String, Any?>> = arrayListOf()
        val profiles = profileRepository.findAllByDeletedAtIsNull()
        for (profile in profiles) {
            val sp = profile.getSpeciality()
            if (sp.isNotEmpty()) {
                val i = speciality.filter { p -> p["id"] == sp.first()["id"]}
                if (i.isEmpty()) {
                    speciality.add(mutableMapOf("id" to sp.first()["id"], "value" to StringEscapeUtils.unescapeHtml(sp.first()["value"].toString())))
                }
            }

            val un = profile.getUniversity()
            if (un.isNotEmpty()) {
                val i = university.filter { p -> p["id"] == un.first()["id"]}
                if (i.isEmpty()) {
                    university.add(mutableMapOf("id" to un.first()["id"], "value" to StringEscapeUtils.unescapeHtml(un.first()["value"].toString())))
                }
            }

            val ct = profile.getCity()
            if (ct.isNotEmpty()) {
                val i = city.filter { p -> p["id"] == ct.first()["id"]}
                if (i.isEmpty()) {
                    city.add(mutableMapOf("id" to ct.first()["id"], "value" to StringEscapeUtils.unescapeHtml(ct.first()["value"].toString())))
                }
            }
        }
        return ResponseEntity(mutableMapOf(
            "university" to university,
            "speciality" to speciality,
            "city" to city,
        ), HttpStatus.OK)
    }

}
