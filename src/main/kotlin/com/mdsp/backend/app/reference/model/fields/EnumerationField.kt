package com.mdsp.backend.app.reference.model.fields

import com.mdsp.backend.app.system.model.Util

class EnumerationField(name: String, config: MutableMap<String, Any?>): ComplexField(name, config) {

    override fun newField() {
        super.newField()
        this.config["defaultValue"] = null
        this.config["values"] = arrayOf(mutableMapOf("id" to "1"), mutableMapOf("value" to "New value"))
        this.config["isSingle"] = true
        this.config["separator"] = ", "
    }

    override fun setConfig() {
        super.setConfig()
        for (item in this.config) {
            when (item.key) {
                "defaultValue" -> {
                    this.setValue(item.value)
                    item.setValue(item.value)
                }
                "values" -> {
                    var hasError = false
                    for (itemIn in (item.value as ArrayList<*>)) {
                        val itemInM = itemIn as MutableMap<*, *>
                        if (itemInM["value"].toString().isEmpty()) {
                            this.error += "Значения пустой\n"
                            break
                        }
                        for (itemOut in (item.value as ArrayList<*>)) {
                            val itemOutM = itemOut as MutableMap<*, *>
                            if (itemInM["id"] != itemOutM["id"]
                                && itemInM["value"] == itemOutM["value"]
                                && !hasError
                            ) {
                                this.error += "Повторяется следующее значение: " + itemInM["value"].toString() + "\n"
                                hasError = true
                                break
                            }
                        }
                        if (hasError) break
                    }
                }
                "isSingle" -> {
                    item.setValue(item.value)
                }
                "separator" -> {
                    if (!(this.config["isSingle"] as Boolean)) {
                        item.setValue(item.value)
                    } else {
                        item.setValue(null)
                    }

                }
            }
        }
    }

}
