package com.mdsp.backend.app.mail.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*
import javax.mail.*
import javax.mail.internet.*

@Service
class GMailService {
    @Value("\${spring.mail.username}")
    private val username: String = ""
    @Value("\${spring.mail.password}")
    private val password: String = ""

    fun sendMessage(email: String, message: String) {
        val userName =  username
        val password =  password
        // FYI: passwords as a command arguments isn't safe
        // They go into your bash/zsh history and are visible when running ps

        val emailFrom = username
        val emailTo = email
        val emailCC = email

        val subject = "Subject"
        val text = message

        val props = Properties()
        putIfMissing(props, "mail.smtp.host", "smtp.gmail.com")
        putIfMissing(props, "mail.smtp.port", "587")
        putIfMissing(props, "mail.smtp.auth", "true")
        putIfMissing(props, "mail.smtp.starttls.enable", "true")

        val session = Session.getDefaultInstance(props, object : javax.mail.Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(userName, password)
            }
        })

        session.debug = true

        try {
            val mimeMessage = MimeMessage(session)
            mimeMessage.setFrom(InternetAddress(emailFrom))
            mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailTo, false))
            mimeMessage.setRecipients(Message.RecipientType.CC, InternetAddress.parse(emailCC, false))
            mimeMessage.setText(text)
            mimeMessage.subject = subject
            mimeMessage.sentDate = Date()

            val smtpTransport = session.getTransport("smtp")
            smtpTransport.connect()
            smtpTransport.sendMessage(mimeMessage, mimeMessage.allRecipients)
            smtpTransport.close()
        } catch (messagingException: MessagingException) {
            messagingException.printStackTrace()
        }
    }

    private fun putIfMissing(props: Properties, key: String, value: String) {
        if (!props.containsKey(key)) {
            props[key] = value
        }
    }
}
