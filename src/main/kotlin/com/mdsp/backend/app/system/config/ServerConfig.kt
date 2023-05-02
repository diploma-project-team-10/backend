package com.mdsp.backend.app.system.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "server")
class ServerConfig {
    private var cors: Cors? = null

    fun getCors() = cors

    fun setCors(cors: Cors) {
        this.cors = cors
    }

    class Cors {
        private var origins: List<String> = ArrayList()

        fun getOrigins() = origins

        fun setOrigins(origins: List<String>) {
            this.origins = origins
        }
    }
}
