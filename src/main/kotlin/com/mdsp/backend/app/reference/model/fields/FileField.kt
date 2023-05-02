package com.mdsp.backend.app.reference.model.fields

import com.mdsp.backend.app.system.model.Util

class FileField(name: String, config: MutableMap<String, Any?>): ComplexField(name, config) {

    override fun newField() {
        super.newField()
        this.config["defaultValue"] = null
        this.config["values"] = arrayOf(mutableMapOf("id" to "1"), mutableMapOf("value" to "New value"))
        this.config["isSingle"] = true
        this.config["isUnique"] = false
        this.config["convertToPdf"] = false
        this.config["enableProtection"] = false
        this.config["enableScanning"] = false
        this.config["maxSize"] = 0
        this.config["maxCount"] = 0
        this.config["maxTotalSize"] = 0
        this.config["separator"] = ", "
        this.config["extension"] = ""
        this.config["renameFile"] = ""
        this.config["filterName"] = ""
    }

    override fun setConfig() {
        super.setConfig()
        for (item in this.config) {
            when (item.key) {
                "defaultValue" -> {
                    this.setValue(item.value)
                    item.setValue(item.value)
                }
                "isSingle", "isUnique", "convertToPdf", "enableProtection", "enableScanning" -> {
                    item.setValue(item.value)
                }
                "renameFile", "filterName", "extension" -> {
                    item.setValue(item.value)
                }
                "separator" -> {
                    if (!(this.config["isSingle"] as Boolean)) {
                        item.setValue(item.value)
                    } else {
                        item.setValue(null)
                    }

                }
                "maxSize", "maxCount", "maxTotalSize" -> {
                    item.setValue(item.value.toString().toLongOrNull())
                }
            }
        }
    }

}
