package com.mdsp.backend.app.structure.controller

import com.mdsp.backend.app.profile.repository.IProfileRepository
import com.mdsp.backend.app.profile.service.ProfileService
import com.mdsp.backend.app.structure.model.Structure
import com.mdsp.backend.app.structure.repository.IStructureRepository
import com.mdsp.backend.app.structure.service.StructureService
import com.mdsp.backend.app.user.model.UserPrincipal
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import kotlin.collections.ArrayList

@RestController
@RequestMapping("/api/structure/manager")
class StructureManagerController() {

    @Autowired
    lateinit var profileRepository: IProfileRepository

    @Autowired
    lateinit var structureRepository: IStructureRepository

    @Autowired
    lateinit var profileService: ProfileService

    @Autowired
    lateinit var structureService: StructureService

    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    fun getManagerSubdivision(authentication: Authentication): ArrayList<Structure> {
        return structureService.getManagerSubdivision((authentication.principal as UserPrincipal).id)
    }

}
