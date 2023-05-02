package com.mdsp.backend.app.reference.model.fields

import com.mdsp.backend.app.system.model.Util

class ReferenceField(name: String, config: MutableMap<String, Any?>): ComplexField(name, config) {

    override fun newField() {
        super.newField()
        this.config["isUnique"] = false
        this.config["defaultValue"] = null
        this.config["referenceId"] = null
        this.config["isSingle"] = false
        this.config["separator"] = ", "
        this.config["limit"] = 0
        this.config["disableLink"] = false
        this.config["fields"] = null
        this.config["defaultGroup"] = null
        this.config["defaultSort"] = null
        this.config["templateView"] = null
        this.config["enableNumbered"] = false
    }

    override fun setConfig() {
        super.setConfig()
        for (item in this.config) {
            when (item.key) {
                "referenceId" -> {
                    if (item.value.toString().isEmpty()) {
                        this.error += ""
                    }
                    item.setValue(item.value)
                }
                "isUnique", "isSingle", "disableLink", "enableNumbered" -> {
                    item.setValue(item.value as Boolean)
                }
                "separator" -> {
                    if (!(this.config["isSingle"] as Boolean)) {
                        item.setValue(item.value)
                    } else {
                        item.setValue(", ")
                    }

                }
                "limit" -> {
                    item.setValue(item.value.toString().toIntOrNull())
                }
                "defaultGroup",
                "fields" -> {
                    item.setValue(item.value)
                }
                "defaultSort" -> {
                    item.setValue(item.value)
                }
                "templateView" -> {
                    item.setValue(item.value)
                }
                "defaultValue" -> {
                    this.setValue(item.value)
                    item.setValue(item.value)
                }
            }
        }
    }
}
