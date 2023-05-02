package com.mdsp.backend.app.reference.model.fields

import com.mdsp.backend.app.system.model.Util

class StructureField(name: String, config: MutableMap<String, Any?>): ComplexField(name, config) {

    override fun newField() {
        super.newField()
        this.config["isUnique"] = false
        this.config["defaultValue"] = null
        this.config["isSingle"] = false
        this.config["isActive"] = true
        this.config["separator"] = ", "
        this.config["limit"] = -1
        this.config["disableLink"] = false
        this.config["fields"] = null
        this.config["defaultGroup"] = null
        this.config["defaultSort"] = null
        this.config["templateView"] = null
        this.config["enableNumbered"] = false
        this.config["enableSubdivision"] = false
        this.config["enableGroup"] = false
        this.config["hideAll"] = false

//        filters?: any[];
    }

    override fun setConfig() {
        super.setConfig()
        for (item in this.config) {
            when (item.key) {
                "isUnique",
                "isSingle",
                "isActive",
                "disableLink",
                "enableNumbered",
                "enableSubdivision",
                "enableGroup",
                "hideAll" -> {
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
