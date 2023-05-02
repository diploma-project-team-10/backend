package com.mdsp.backend.app.mail.model

import java.util.*


class Mail {
    private var from: String? = null
    private var to: String? = null
    private var subject: String? = null
    private var content: String? = null
    private var model: MutableMap<String, String>

    constructor() {
        model = HashMap()
    }

    constructor(from: String, to: String, subject: String, content: String, model: MutableMap<String, String>) {
        this.from = from
        this.to = to
        this.subject = subject
        this.content = content
        this.model = model
    }

    fun getFrom() = this.from
    fun setFrom(from: String) {
        this.from = from
    }

    fun getTo() = this.to
    fun setTo(to: String?) {
        this.to = to
    }

    fun getSubject() = this.subject
    fun setSubject(subject: String?) {
        this.subject = subject
    }

    fun getContent() = this.content
    fun setContent(content: String?) {
        this.content = content
    }

    fun getModel() = this.model
    fun setModel(model: MutableMap<String, String>) {
        this.model = model
    }

}
