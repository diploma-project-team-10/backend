package com.mdsp.backend.app.structure.controller

import com.mdsp.backend.app.profile.repository.IProfileRepository
import com.mdsp.backend.app.profile.service.ProfileService
import com.mdsp.backend.app.structure.model.Structure
import com.mdsp.backend.app.structure.repository.IStructureRepository
import com.mdsp.backend.app.structure.service.StructureService
import com.mdsp.backend.app.system.model.Status
import com.mdsp.backend.app.user.model.UserPrincipal
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.sql.Timestamp
import java.util.*
import javax.validation.Valid
import kotlin.collections.ArrayList

@RestController
@RequestMapping("/api/structure")
class StructureController() {

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
    fun getStructureByParent(@RequestParam(value = "pid") parentId: UUID? = null): ArrayList<Structure> {
        val parent = structureRepository.findAllByParentIdAndDeletedAtIsNullOrderBySortOrderAsc(parentId)
        for (children in parent) {
            children.childrenStructure = structureRepository.findAllByParentIdAndDeletedAtIsNullOrderBySortOrderAsc(children.getId())
            children.childrenCount = children.childrenStructure!!.size
            for (child in children.childrenStructure!!) {
                child.childrenCount = structureRepository.countAllByParentIdAndDeletedAtIsNull(child.getId())
            }
        }
        return structureRepository.findAllByParentIdAndDeletedAtIsNullOrderBySortOrderAsc(parentId)
    }

    @GetMapping("/list/expanded")
    @PreAuthorize("isAuthenticated()")
    fun getStructureAll(@RequestParam(value = "pid") parentId: UUID? = null): ArrayList<Structure> {
        val grandParent = structureRepository.findAllByParentIdAndDeletedAtIsNullOrderBySortOrderAsc(parentId)
        for (children in grandParent) {
            children.childrenStructure = getStructureAll(children.getId())
        }
        return grandParent
    }

    @GetMapping("/get/{id}")
    @PreAuthorize("isAuthenticated()")
    fun getStructureBId(@PathVariable(value = "id") id: UUID): Optional<Structure> {
        return structureRepository.findByIdAndDeletedAtIsNull(id)
    }

    @PostMapping("/edit")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CHIEF') or hasRole('HEAD_CITY')")
    fun createStructure(@Valid @RequestBody newStructure: Structure, authentication: Authentication): ResponseEntity<*> {
        val status = Status()
        status.message = "Group already exist!"
        var isNew = false
        var structure: Optional<Structure> = Optional.empty()
        if (newStructure.getDisplayName().isNullOrEmpty()) {
            status.message = "Title must be blank!"
            return ResponseEntity(status, HttpStatus.OK)
        }

        if (newStructure.getId() != null) {
            structure = structureRepository.findByIdAndDeletedAtIsNull(newStructure.getId()!!)
            if (!structure.isPresent) {
                status.status = 0
                status.message = "Group already exist!"
                return ResponseEntity(status, HttpStatus.OK)
            }
            if(structure.get().getParentId() == null && newStructure.parent != null) {
                status.status = 0
                status.message = "Can't set Parent to Main Group"
                return ResponseEntity(status, HttpStatus.OK)
            }
            newStructure.editor = (profileService.getProfileReferenceById((authentication.principal as UserPrincipal).id))
        } else {
            newStructure.setSortOrder(structureRepository.countAllByParentIdAndDeletedAtIsNull(newStructure.getParentId()))
            newStructure.creator = (profileService.getProfileReferenceById((authentication.principal as UserPrincipal).id))
            isNew = true
        }
        //ParentId
        if (newStructure.parent != null) {
            for (item in newStructure.parent!!) {
                newStructure.setParentId(UUID.fromString(item["id"].toString()))
                break
            }
        }
        if (newStructure.getId() != null && newStructure.getParentId() != null) {
            val newStructureInDb = structureRepository.findByIdAndDeletedAtIsNull(newStructure.getId()!!)
            val recursiveStructure = structureRepository.findByIdAndDeletedAtIsNull(newStructure.getParentId()!!)
            if(newStructureInDb.isPresent && newStructure.getId() == newStructure.getParentId()) {
                newStructure.setParentId(newStructureInDb.get().getParentId())
            } else if (newStructureInDb.isPresent && recursiveStructure.isPresent && structureService.isRecursive(newStructure.getId()!!, newStructure.getParentId()!!)) {
                recursiveStructure.get().setParentId(newStructureInDb.get().getParentId())
                structureRepository.save(recursiveStructure.get())
            }
        }

        if (newStructure.getId() != null) {
            newStructure.setPath(structureService.getPath(newStructure).reversedArray())
        } else {
            structureRepository.save(newStructure)
            newStructure.setPath(structureService.getPath(newStructure).reversedArray())
        }
        structureRepository.save(newStructure)

        //Manager create Profile Structure
        val newStructureEmployees = newStructure.getEmployee()
        newStructureEmployees.addAll(newStructure.getManager())
        newStructure.setEmployee(newStructureEmployees)
        if (newStructure.getManager().isNotEmpty()) {
            val profileManager =
                profileRepository.findByIdAndDeletedAtIsNull(UUID.fromString(newStructure.getManager()[0]["id"].toString()))
            if (!profileManager.isPresent) {
                status.message = "Profile doesn't exist!"
                return ResponseEntity(status, HttpStatus.OK)
            }

            if (!structureRepository.existsByParentIdAndProfileIdAndType(newStructure.getId()!!, profileManager.get().getId()!!, "profile")) {
                var profileStructure = Structure(null)

                profileStructure.setProfileId(profileManager.get().getId())

                profileStructure.creator = (profileService.getProfileReferenceById((authentication.principal as UserPrincipal).id))
                profileStructure.setType("profile")
                profileStructure.setParentId(newStructure.getId())
                profileStructure.setDisplayName(profileManager.get().getFio())
                profileStructure.setSortOrder(0)
                structureRepository.save(profileStructure)

                profileStructure.setPath(structureService.getPath(profileStructure).reversedArray())
                structureRepository.save(profileStructure)
            } else {
                val manager = structureRepository.findAllByParentIdAndProfileIdAndType(newStructure.getId()!!, profileManager.get().getId()!!, "profile")
                if (manager.isNotEmpty() && manager[0].deletedAt != null) {
                    manager[0].deletedAt = (null)
                    manager[0].updatedAt = (Timestamp(System.currentTimeMillis()))
                    manager[0].editor = (profileService.getProfileReferenceById((authentication.principal as UserPrincipal).id))
                    structureRepository.save(manager[0])
                }
            }
        }

        status.status = 1
        status.message = "Group created!"
        status.value = newStructure
        return ResponseEntity(status, HttpStatus.OK)
    }

    @PostMapping("/edit/employee/{parentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CHIEF') or hasRole('HEAD_CITY')")
    fun createEmployees(
        @PathVariable(value = "parentId") parentId: UUID,
        @Valid @RequestBody newStructure: Structure,
        authentication: Authentication
    ): ResponseEntity<*> {
        var status = Status()

        val structure = structureRepository.findByIdAndDeletedAtIsNull(parentId)
        if (!structure.isPresent) {
            status.message = "Structure doesn't exist!"
            return ResponseEntity(status, HttpStatus.OK)
        }

        val employeesStr = structure.get().getManager()
        employeesStr.addAll(structure.get().getEmployee())

        for (item in newStructure.getEmployee()) {
            val employee = Structure(null)
            val profile = profileRepository.findByIdAndDeletedAtIsNull(UUID.fromString(item["id"].toString()))
            if (profile.isPresent) {
                if (structureRepository.existsByParentIdAndProfileIdAndType(structure.get().getId()!!, profile.get().getId()!!, "profile")) {
                    val structureEmployee = structureRepository.findAllByParentIdAndProfileIdAndType(
                        structure.get().getId()!!, profile.get().getId()!!, "profile"
                    )
                    if (structureEmployee.isNotEmpty() && structureEmployee[0].deletedAt != null) {
                        structureEmployee[0].deletedAt = (null)
                        structureRepository.save(structureEmployee[0])
                    }
                    continue
                }
                employee.setProfileId(profile.get().getId())
                employee.setDisplayName(item["value"].toString())
                employee.setParentId(structure.get().getId())
                employee.setType("profile")
                employee.setSortOrder(structureRepository.countAllByParentIdAndDeletedAtIsNull(structure.get().getId()))
                employee.creator = (profileService.getProfileReferenceById((authentication.principal as UserPrincipal).id))

                structureRepository.save(employee)
                employee.setPath(structureService.getPath(employee).reversedArray())

                structureRepository.save(employee)

                if (employee.getId() != null) {
                    employeesStr.add(item)
                }
            }
        }
        structure.get().setEmployee(employeesStr)
        structure.get().editor = (profileService.getProfileReferenceById((authentication.principal as UserPrincipal).id))
        structureRepository.save(structure.get())

        status.status = 1
        status.message = "Employee added successfully!"
        status.value = structure
        return ResponseEntity(status, HttpStatus.OK)
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CHIEF') or hasRole('HEAD_CITY')")
    fun deleteBooks(@PathVariable(value = "id") id: UUID): ResponseEntity<*> {
        val status = Status()
        status.status = 0
        status.message = ""
        if (id == UUID.fromString("00000000-0000-0000-0000-000000000000")) {
            status.message = "You can't delete root structure"
            return ResponseEntity(status, HttpStatus.OK)
        }

        val structure = structureRepository.findByIdAndDeletedAtIsNull(id)
        if (structure.isPresent && structure.get().getType() == "profile" && structure.get().getParentId() != null) {
            val parentStructure = structureRepository.findByIdAndDeletedAtIsNull(structure.get().getParentId()!!)
            if (
                parentStructure.isPresent
                && parentStructure.get().getManager().isNotEmpty()
                && parentStructure.get().getManager()[0].containsKey("id")
                && parentStructure.get().getManager()[0]["id"].toString() == structure.get().getProfileId().toString()
            ) {
                parentStructure.get().setManager(arrayListOf())
                structureRepository.save(parentStructure.get())
            }
            if (
                parentStructure.isPresent
                && parentStructure.get().getEmployee().isNotEmpty()
            ) {
                var employees = parentStructure.get().getEmployee()
                employees = employees.filter { it -> it["id"]!!.toString() != structure.get().getProfileId().toString() } as ArrayList<MutableMap<String, Any?>>
                parentStructure.get().setEmployee(employees)
                structureRepository.save(parentStructure.get())
            }
        }

        val structures = structureRepository.findAllByIdOrParentIdAndDeletedAtIsNull(id, id)

        deleteRecursive(structures)
        status.status = 1
        status.message = "Structure deleted!"
        return ResponseEntity(status, HttpStatus.OK)
    }

    fun deleteRecursive(structures: ArrayList<Structure>) {
        for (item in structures) {
            item.deletedAt = (Timestamp(System.currentTimeMillis()))
            structureRepository.save(item)
            val structuresParent = structureRepository.findAllByParentIdAndDeletedAtIsNullOrderBySortOrderAsc(item.getId()!!)
            if (structuresParent.isNotEmpty()) {
                deleteRecursive(structuresParent)
            }
        }
    }
//
//    @GetMapping("/fix")
//    fun update(): Status {
//        val status = Status()
//        val structures = structureRepository.findAll()
//        for (structure in structures) {
//            if (structure.getManager().isNotEmpty()) {
//                val manager = UUID.fromString(structure.getManager().first()["id"].toString())
//                val structuresManager = structureRepository.findAllByProfileIdAndDeletedAtIsNull(manager)
//                for (str in structuresManager) {
//                    structureRepository.fix(str.getId()!!, str.getParentId()!!)
//                }
//            }
//        }
//        return status
//    }
}
