package com.mdsp.backend.app.reference.model.fields

abstract class Field {
    protected var name: String = ""
    protected var config: MutableMap<String, Any?> = mutableMapOf()
    protected var error: String = ""
    protected var isEmpty: Boolean = false
    protected var valueField: Any? = null
    protected var isConfig: Boolean = false
    protected var isComplexField: Boolean = false

    constructor(name: String, config: MutableMap<String, Any?>) {
        this.name = name
        this.config = config
    }

    abstract fun setValue(valueField: Any?)

    fun getValue() = this.valueField

    @JvmName("getConfigData")
    fun getConfig() = this.config

    @JvmName("getErrorMsg")
    fun getError() = this.error

    open fun newField() {
        this.config["title"] = ""
        this.config["hint"] = ""
        this.config["isRequired"] = false
        this.config["orderNum"] = 999
    }

    open fun setDefaultValue() {
        if (this.config.contains(this.config["defaultValue"])) {
            this.setValue(this.config["defaultValue"])
        } else {
            this.setValue(null)
        }
    }

    fun setOrderNum(orderNum: Int) {
        this.config["orderNum"] = orderNum
    }

    fun setIsConfig(isConfig: Boolean) {
        this.isConfig = isConfig;
    }

    open fun setConfig() {
        for (item in this.config) {
            when (item.key) {
                "title" -> {
                    if (item.value == null || item.value.toString().trim().isEmpty()) {
                        this.error += "Название обязательно для заполнения!"
                    }

                    item.setValue(item.value.toString().trim())
                }
                "hint" -> {
                    if (item.value != null) {
                        item.setValue(item.value.toString().trim())
                    } else {
                        item.setValue(null)
                    }
                }
                "isRequired" -> {
                    item.setValue(item.value)
                }
            }
        }
    }

    open fun serialized() = this.valueField

    open fun unserialized(serialized: Any?) {
        if (serialized != null) {
            this.valueField = serialized.toString().trim()
        } else {
            this.valueField = null
        }
        this.isEmpty = !(serialized != null && serialized.toString().isNotEmpty())
    }

    open fun unserializedToString(): String? {
        if (this.valueField != null) {
           return this.valueField.toString().trim()
        }
        return null
    }

}
