package com.mdsp.backend.app.user.exception

class ResourceAlreadyInUseException: RuntimeException {
    private var resourceName: String? = null
    private var fieldName: String? = null

    @Transient
    private var fieldValue: String? = null

    constructor(resourceName: String, fieldName: String, fieldValue: String): super(kotlin.String.format("%s already in use with %s : '%s'", resourceName, fieldName, fieldValue)) {
        this.resourceName = resourceName
        this.fieldName = fieldName
        this.fieldValue = fieldValue
    }

}