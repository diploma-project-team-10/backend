package com.mdsp.backend.app.system.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "client")
class FeignUserConfig {
    private var urls: List<String> = ArrayList()
    private var head: String = ""
    private var hosts: Map<String, String> = mutableMapOf()

    fun getUrls() = urls
    fun setUrls(urls: List<String>) {
        this.urls = urls
    }

    fun getHead() = head
    fun setHead(head: String) {
        this.head = head
    }

    fun getHosts() = hosts
    fun setHosts(hosts: Map<String, String>) {
        this.hosts = hosts
    }
}
