package com.mdsp.backend.app.course.controller

import com.mdsp.backend.app.system.model.Status
import com.mdsp.backend.app.course.model.Company
import com.mdsp.backend.app.course.repository.ICompanyRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.Valid


@RestController
@RequestMapping("/api/project/course/company")

class CompanyController {
    @Autowired
    lateinit var companyRepository: ICompanyRepository


    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    fun getCompanies() = companyRepository.findAllByDeletedAtIsNull()

    @GetMapping("/list-page")
    @PreAuthorize("isAuthenticated()")
    fun getCompaniesPage(
        @RequestParam(value = "page") page: Int = 1,
        @RequestParam(value = "size") size: Int = 20
    ): Page<Company>? {
        val pageImp: PageRequest = PageRequest.of(page - 1, size)
        return companyRepository.findAllByDeletedAtIsNull(pageImp)
    }

    @GetMapping("/get/{id}")
    @PreAuthorize("isAuthenticated()")
    fun getCompany(@PathVariable(value = "id") id: UUID) = companyRepository.findByIdAndDeletedAtIsNull(id)

    @PostMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    fun createCompany(@Valid @RequestBody newCompany: Company): ResponseEntity<*> {
        val status = Status()
        companyRepository.save(newCompany)

        status.status = 1
        status.message = "New Company created!"
        status.value = newCompany.getId()
        return ResponseEntity(status, HttpStatus.OK)
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun editCompany(
        @PathVariable(value = "id") id: UUID,
        @Valid @RequestBody newCompany: Company
    ): ResponseEntity<*> {
        val status = Status()
        status.message = "Company doesn't save!"
        if (newCompany.getId() == id && companyRepository.findByIdAndDeletedAtIsNull(id).isPresent) {
            companyRepository.save(newCompany)

            status.status = 1
            status.message = "New Company created!"
            status.value = newCompany.getId()
        }

        return ResponseEntity(status, HttpStatus.OK)
    }
}
