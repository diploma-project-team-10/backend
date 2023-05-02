package com.mdsp.backend.app.user.event.listener

import com.mdsp.backend.app.mail.service.MailService
import com.mdsp.backend.app.profile.repository.IProfileRepository
import com.mdsp.backend.app.user.event.OnRegenerateEmailVerificationEvent
import com.mdsp.backend.app.user.exception.MailSendException
import freemarker.template.TemplateException
//import org.apache.log4j.Logger
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.io.IOException
import javax.mail.MessagingException

@Component
class OnRegenerateEmailVerificationListener: ApplicationListener<OnRegenerateEmailVerificationEvent> {

    @Autowired
    lateinit var profileRepository: IProfileRepository

    private var logger = LogManager.getLogger(OnGenerateResetLinkEventListener::class.java)

    //    private var logger = Logger.getLogger(OnRegenerateEmailVerificationListener::class.java)
    private var mailService: MailService? = null

    @Autowired
    constructor(mailService: MailService) {
        this.mailService = mailService;
    }

    @Async
    override fun onApplicationEvent(onRegenerateEmailVerificationEvent: OnRegenerateEmailVerificationEvent) {
        resendEmailVerification(onRegenerateEmailVerificationEvent)
    }

    fun resendEmailVerification(event: OnRegenerateEmailVerificationEvent) {
        var profileId = event.getProfileId()
        val profileCandidate = profileRepository.findByIdAndDeletedAtIsNull(profileId!!)

        var emailVerificationEvent = event.getToken()
        var recipientAddress = profileCandidate.get().getEmail()

        var emailConfirmationUrl = event.getRedirectUrl()!!.queryParam("token", emailVerificationEvent!!.getToken()).toUriString()

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
