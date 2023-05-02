package com.mdsp.backend.app.reference.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

class ObjectAccess {
    var id: UUID? = null
    var title: String = ""

    @JsonProperty("may_view")
    var mayView: Boolean = false

    @JsonProperty("may_edit")
    var mayEdit: Boolean = false

    @JsonProperty("may_delete")
    var mayDelete: Boolean = false
}
