package com.mdsp.backend.app.system.config

import io.swagger.v3.oas.models.OpenAPI
import org.springdoc.core.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class SwaggerConfig {

    @Bean
    fun publicAllApi(): GroupedOpenApi? {
        return GroupedOpenApi.builder()
            .group("Api")
            .pathsToMatch("/api/**")
            .build()
    }

    @Bean
    fun publicAuthApi(): GroupedOpenApi? {
        return GroupedOpenApi.builder()
            .group("Auth")
            .pathsToMatch("/api/auth/**")
            .build()
    }

    @Bean
    fun publicV2ProfilesApi(): GroupedOpenApi? {
        return GroupedOpenApi.builder()
            .group("Profiles v2")
            .pathsToMatch("/api/v2/profiles/**")
            .build()
    }

    @Bean
    fun publicStatistics(): GroupedOpenApi? {
        return GroupedOpenApi.builder()
            .group("Dashboard Statistics")
            .pathsToMatch("/api/statistics/all/**")
            .build()
    }

    @Bean
    fun publicStatisticsProfile(): GroupedOpenApi? {
        return GroupedOpenApi.builder()
            .group("Profile Statistics")
            .pathsToMatch("/api/project/grade/**")
            .build()
    }

    @Bean
    fun publicDashboard(): GroupedOpenApi? {
        return GroupedOpenApi.builder()
            .group("Dashboard Page")
            .pathsToMatch("/api/dashboard/**")
            .build()
    }

    @Bean
    fun publicAnalytics(): GroupedOpenApi? {
        return GroupedOpenApi.builder()
            .group("Analytics Page")
            .pathsToMatch("/api/analytics/**")
            .build()
    }

    @Bean
    fun publicNotify(): GroupedOpenApi? {
        return GroupedOpenApi.builder()
            .group("Notify Page")
            .pathsToMatch("/api/notify/**")
            .build()
    }

    @Bean
    fun publicNotifyReading(): GroupedOpenApi? {
        return GroupedOpenApi.builder()
            .group("Notify Reading")
            .pathsToMatch("/api/project/reading/notify/**")
            .build()
    }

    @Bean
    fun customOpenApi(): OpenAPI? {
        return null
//        return OpenAPI().info(
//            Info().title("Application API")
//                .version(appVersion)
//                .description(appDescription)
//                .license(
//                    License().name("Apache 2.0")
//                        .url("http://springdoc.org")
//                )
//                .contact(
//                    Contact().name("username")
//                        .email("test@gmail.com")
//                )
//        )
//            .servers(
//                List.of(
//                    Server().url("http://localhost:8080")
//                        .description("Dev service"),
//                    Server().url("http://localhost:8082")
//                        .description("Beta service")
//                )
//            )
    }
}
