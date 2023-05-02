package com.mdsp.backend.app.course.controller

import com.mdsp.backend.app.course.model.PackageCourse
import com.mdsp.backend.app.course.repository.IPackageRepository
import com.mdsp.backend.app.system.model.Status
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.Valid


@RestController
@RequestMapping("/api/project/course/package")

class PackageController {
    @Autowired
    lateinit var packageRepository: IPackageRepository


    @GetMapping("/list")
    //@PreAuthorize("isAuthenticated()")
    fun getPackages() = packageRepository.findAllByDeletedAtIsNull()

    @GetMapping("/list-page")
    fun getPackagesPage(
        @RequestParam(value = "page") page: Int = 1,
        @RequestParam(value = "size") size: Int = 20
    ): Page<PackageCourse>? {
        val pageImp: PageRequest = PageRequest.of(page - 1, size)
        return packageRepository.findAllByDeletedAtIsNull(pageImp)
    }

    @GetMapping("/get/{id}")
//    @PreAuthorize("isAuthenticated()")
    fun getPackage(@PathVariable(value = "id") id: UUID) = packageRepository.findByIdAndDeletedAtIsNull(id)

    @PostMapping("/new")
    //@PreAuthorize("hasRole('ADMIN')")
    fun createPackage(@Valid @RequestBody newPackage: PackageCourse): ResponseEntity<*> {
        val status = Status()
        packageRepository.save(newPackage)

        status.status = 1
        status.message = "New Company created!"
        status.value = newPackage.getId()
        return ResponseEntity(status, HttpStatus.OK)
    }

    @PostMapping("/edit/{id}")
    //@PreAuthorize("hasRole('ADMIN')")
    fun editPackage(
        @PathVariable(value = "id") id: UUID,
        @Valid @RequestBody newPackage: PackageCourse
    ): ResponseEntity<*> {
        val status = Status()
        status.message = "Company doesn't save!"
        if (newPackage.getId() == id && packageRepository.findByIdAndDeletedAtIsNull(id).isPresent) {
            packageRepository.save(newPackage)

            status.status = 1
            status.message = "New Company created!"
            status.value = newPackage.getId()
        }

        return ResponseEntity(status, HttpStatus.OK)
    }
}
