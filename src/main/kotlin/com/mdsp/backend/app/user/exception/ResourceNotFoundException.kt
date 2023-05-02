package com.mdsp.backend.app.user.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND)
class ResourceNotFoundException: RuntimeException {
    private var resourceName: String? = null
    private var fieldName: String? = null
    private var fieldValue: Any? = null

    constructor(resourceName: String, fieldName: String, fieldValue: String): super(kotlin.String.format("%s not found with %s : '%s'", resourceName, fieldName, fieldValue)) {
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
}