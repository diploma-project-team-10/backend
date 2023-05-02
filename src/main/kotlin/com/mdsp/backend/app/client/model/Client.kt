package com.mdsp.backend.app.client.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.mdsp.backend.app.system.model.DateAudit
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "clients")
class Client: DateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private var id: UUID? = null

    @Column(name = "title")
    private var title: String = ""

    @Column(name = "url")
    private var url: String = ""

    constructor(id: UUID?) {
        this.id = id
    }

    fun getId() = this.id
    fun getTitle() = this.title
    fun getUrl() = this.url

    fun setId(id: UUID?) { this.id = id }
    fun setTitle(title: String) { this.title = title }
    fun setUrl(url: String) { this.url = url }

}
