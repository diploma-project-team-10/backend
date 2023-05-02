package com.mdsp.backend.app.user.event.listener

import com.mdsp.backend.app.mail.service.MailService
import com.mdsp.backend.app.profile.repository.IProfileRepository
import com.mdsp.backend.app.user.event.OnUserAccountChangeEvent
import com.mdsp.backend.app.user.exception.MailSendException
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
class OnUserAccountChangeListener: ApplicationListener<OnUserAccountChangeEvent> {
    @Autowired
    lateinit var profileRepository: IProfileRepository

//    private var logger = Logger.getLogger(OnUserAccountChangeListener::class.java)
private var logger = LogManager.getLogger(OnGenerateResetLinkEventListener::class.java)

    private var mailService: MailService? = null

    @Autowired
    constructor(mailService: MailService) {
        this.mailService = mailService
    }

    @Async
    override fun onApplicationEvent(onUserAccountChangeEvent: OnUserAccountChangeEvent) {
        sendAccountChangeEmail(onUserAccountChangeEvent)
    }

    fun sendAccountChangeEmail(event: OnUserAccountChangeEvent) {
        var profileId = event.getProfileId()
        var profileCandidate = profileRepository.findByIdAndDeletedAtIsNull(profileId!!)
        if(!profileCandidate.isPresent) { throw RuntimeException() }

        var action = event.getAction()
        var actionStatus = event.getActionStatus()
        var recipientAddress = profileCandidate.get().getEmail()

        try {
            mailService!!.sendAccountChangeEmail(action, actionStatus, recipientAddress)
        } catch (e: IOException) {
            logger.error(e)
            throw MailSendException(recipientAddress, "Account Change Mail")
        } catch (e: TemplateException) {
            logger.error(e)
            throw MailSendException(recipientAddress, "Account Change Mail")
        } catch (e: MessagingException) {
            logger.error(e)
            throw MailSendException(recipientAddress, "Account Change Mail")
        }

    }
}
