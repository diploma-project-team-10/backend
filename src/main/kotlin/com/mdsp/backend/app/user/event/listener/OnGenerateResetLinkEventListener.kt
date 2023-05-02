package com.mdsp.backend.app.user.event.listener

import com.mdsp.backend.app.mail.service.MailService
import com.mdsp.backend.app.profile.repository.IProfileRepository
import com.mdsp.backend.app.user.event.OnGenerateResetLinkEvent
import com.mdsp.backend.app.user.exception.MailSendException
import com.mdsp.backend.app.user.model.PasswordResetToken
import freemarker.template.TemplateException
import org.apache.logging.log4j.LogManager
//import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.io.IOException
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.mail.MessagingException


@Component
class OnGenerateResetLinkEventListener: ApplicationListener<OnGenerateResetLinkEvent> {
    @Autowired
    lateinit var profileRepository: IProfileRepository

    private var logger = LogManager.getLogger(OnGenerateResetLinkEventListener::class.java)
    private var mailService: MailService? = null

    @Autowired
    constructor(mailService: MailService?) {
        this.mailService = mailService
    }

    @Async
    override fun onApplicationEvent(onGenerateResetLinkMailEvent: OnGenerateResetLinkEvent) {
        sendResetLink(onGenerateResetLinkMailEvent)
    }

    private fun sendResetLink(event: OnGenerateResetLinkEvent) {
        val passwordResetToken: PasswordResetToken = event.getPasswordResetToken()!!
        val profileId = passwordResetToken.getProfileId()

        val profileCandidate = profileRepository.findByIdAndDeletedAtIsNull(profileId!!)
        if(!profileCandidate.isPresent) { throw RuntimeException() }

        val recipientAddress = profileCandidate.get().getEmail()
        val emailConfirmationUrl: String = URLDecoder.decode(
                event.getRedirectUrl()!!.queryParam("john-wick", passwordResetToken.getToken()).toUriString(),
            StandardCharsets.UTF_8.toString()
        )
        try {
            mailService!!.sendResetLink(emailConfirmationUrl, recipientAddress!!)
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
