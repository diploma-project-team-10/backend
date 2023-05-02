package com.mdsp.backend.app.mail.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.PropertySource
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean
import java.util.*


@Configuration
@EnableAsync
class MailConfig {
    @Value("\${spring.mail.default-encoding}")
    private val mailDefaultEncoding: String? = null

    @Value("\${spring.mail.host}")
    private val mailHost: String? = null

    @Value("\${spring.mail.username}")
    private val mailUsername: String? = null

    @Value("\${spring.mail.password}")
    private val mailPassword: String? = null

    @Value("\${spring.mail.port}")
    private val mailPort: Int? = null

    @Value("\${spring.mail.protocol}")
    private val mailProtocol: String? = null

    @Value("\${spring.mail.debug}")
    private val mailDebug: String? = null

    @Value("\${spring.mail.smtp.auth}")
    private val mailSmtpAuth: String? = null

    @Value("\${spring.mail.smtp.starttls.enable}")
    private val mailSmtpStartTls: String? = null

    @get:Primary
    @get:Bean
    val freeMarkerConfiguration: FreeMarkerConfigurationFactoryBean
        get() {
            val bean = FreeMarkerConfigurationFactoryBean()
            bean.setTemplateLoaderPath("/templates/")
            return bean
        }

    @get:Bean
    val mailSender: JavaMailSender
        get() {
            val mailSender = JavaMailSenderImpl()
            mailSender.host = mailHost
            mailSender.defaultEncoding = mailDefaultEncoding
            mailSender.port = mailPort!!
            mailSender.username = mailUsername
            mailSender.password = mailPassword
            val javaMailProperties = Properties()
            javaMailProperties["mail.smtp.starttls.enable"] = mailSmtpStartTls
            javaMailProperties["mail.smtp.auth"] = mailSmtpAuth
            javaMailProperties["mail.transport.protocol"] = mailProtocol
            javaMailProperties["mail.debug"] = mailDebug
            mailSender.javaMailProperties = javaMailProperties
            return mailSender
        }
}
