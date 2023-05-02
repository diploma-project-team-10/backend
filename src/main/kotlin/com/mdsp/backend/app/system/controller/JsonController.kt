package com.mdsp.backend.app.system.controller

import com.mdsp.backend.app.system.model.Status
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("")
class JsonController() {

    @GetMapping("/json")
    fun getJson(): ResponseEntity<*> {
        var status = Status()
        status.status = 1
        status.message = ""
        return ResponseEntity(status, HttpStatus.OK)
    }

    @GetMapping("/json/version")
    fun getJsonVersion(): ResponseEntity<*> {
        var status = Status()
        status.status = 1
        status.message = "1.0.0"
        return ResponseEntity(status, HttpStatus.OK)
    }

}
