package com.mdsp.backend.app.user.event.listener

import com.mdsp.backend.app.mail.service.MailService
import com.mdsp.backend.app.user.event.OnUserRegistrationCompleteEvent
import com.mdsp.backend.app.user.exception.MailSendException
import com.mdsp.backend.app.user.security.service.EmailVerificationTokenService
import freemarker.template.TemplateException
import org.apache.logging.log4j.LogManager
//import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.io.IOException
import javax.mail.MessagingException

@Component
class OnUserRegistrationCompleteListener: ApplicationListener<OnUserRegistrationCompleteEvent> {
//    private var logger = Logger.getLogger(OnUserRegistrationCompleteListener::class.java)
private var logger = LogManager.getLogger(OnGenerateResetLinkEventListener::class.java)

    private var emailVerificationTokenService: EmailVerificationTokenService? = null
    private var mailService: MailService? = null

        @Autowired
    constructor(emailVerificationTokenService: EmailVerificationTokenService, mailService: MailService) {
        this.emailVerificationTokenService = emailVerificationTokenService;
        this.mailService = mailService;
    }

    @Async
    override fun onApplicationEvent(onUserRegistrationCompleteEvent: OnUserRegistrationCompleteEvent) {
        sendEmailVerification(onUserRegistrationCompleteEvent)
    }

    fun sendEmailVerification(event: OnUserRegistrationCompleteEvent) {
        var user = event.getUser()
        var token: String = emailVerificationTokenService!!.generateNewToken()
        emailVerificationTokenService!!.createVerificationToken(user!!.getId()!!, token)

        var recipientAddress = user.getEmail()
        var emailConfirmationUrl = event.getRedirectUrl()!!.queryParam("token", token).toUriString()

        try {
            mailService!!.sendEmailVerification(emailConfirmationUrl, recipientAddress!!)
        } catch (e: IOException) {
            logger.error(e)
            throw MailSendException(recipientAddress, "Email Verification")
        } catch (e: TemplateException) {
            logger.error(e)
            throw MailSendException(recipientAddress, "Email Verification")
        } catch (e: MessagingException) {
            logger.error(e)
            throw MailSendException(recipientAddress, "Email Verification")
        }
    }


}
