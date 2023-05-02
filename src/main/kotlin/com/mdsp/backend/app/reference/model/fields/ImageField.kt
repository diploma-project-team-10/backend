package com.mdsp.backend.app.reference.model.fields

import com.mdsp.backend.app.system.model.Util

class ImageField(name: String, config: MutableMap<String, Any?>): ComplexField(name, config) {

    override fun newField() {
        super.newField()
        this.config["defaultValue"] = null
        this.config["values"] = arrayOf(mutableMapOf("id" to "1"), mutableMapOf("value" to "New value"))
        this.config["isSingle"] = true
        this.config["isAvatar"] = false
        this.config["maxSize"] = 0
        this.config["maxCount"] = 0
        this.config["thumbX"] = 0
        this.config["thumbY"] = 0
        this.config["separator"] = ", "
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
                "isSingle", "isAvatar" -> {
                    item.setValue(item.value)
                }
                "renameFile", "filterName" -> {
                    item.setValue(item.value)
                }
                "separator" -> {
                    if (!(this.config["isSingle"] as Boolean)) {
                        item.setValue(item.value)
                    } else {
                        item.setValue(null)
                    }

                }
                "maxSize", "maxCount", "thumbX", "thumbY" -> {
                    item.setValue(item.value.toString().toLongOrNull())
                }
            }
        }
    }

}
