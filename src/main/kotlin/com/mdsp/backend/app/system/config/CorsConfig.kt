package com.mdsp.backend.app.system.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@Configuration
@EnableWebMvc
class CorsConfig : WebMvcConfigurer {
    @Autowired
    private var serverConfig: ServerConfig? = null

    override fun addCorsMappings(registry: CorsRegistry) {
        var corsOrigins: Array<String> = arrayOf()
        if (
            serverConfig != null
            && serverConfig!!.getCors() != null
        ) {
            corsOrigins = serverConfig!!.getCors()!!.getOrigins().toTypedArray()
        }
        println("corsOrigins:${corsOrigins.toMutableList()}")
        registry.addMapping("/**")
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowedHeaders("*")
            .allowedOrigins(*corsOrigins)
            .maxAge(3600)
            .allowCredentials(true)
    }
}
