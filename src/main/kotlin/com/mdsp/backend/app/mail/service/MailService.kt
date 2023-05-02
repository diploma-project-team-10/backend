package com.mdsp.backend.app.mail.service


import com.mdsp.backend.app.mail.model.Mail
import freemarker.template.Configuration
import freemarker.template.Template
import freemarker.template.TemplateException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit
import javax.mail.MessagingException
import javax.mail.internet.MimeMessage

@Service
class MailService {
    //TODO REMOVE
    private lateinit var mailSender: JavaMailSender

    @Value("\${mail.tmp.img-dir}")
    private val path: String? = null

    @Value("\${mail.tmp.company-logo}")
    private val companyLogoName: String? = null

    private var templateConfiguration: Configuration = Configuration()

    @Value("\${spring.mail.username}")
    private lateinit var mailFrom: String

    var basePackagePath: String = "/templates/"
    var expiration: Long = 3600000

    @Autowired
    constructor(mailSender: JavaMailSender, templateConfiguration: Configuration) {
        this.mailSender = mailSender
        this.templateConfiguration = templateConfiguration
    }

    constructor() {}

    @Throws(IOException::class, TemplateException::class, MessagingException::class)
    fun sendEmailVerification(emailVerificationUrl: String, to: String) {
        val mail = Mail()
        mail.setSubject("Email Verification [Team CEP]")
        mail.setTo(to)
        mail.setFrom(mailFrom)
        //TO DO
        //Color change by logo
        //mail.getModel().put("color", #HEX_VALUE_FROM_PROPERTIES)
        mail.getModel().put("path", path!!)
        mail.getModel().put("company",companyLogoName!!)
        mail.getModel().put("userName", to)
        mail.getModel().put("userEmailTokenVerificationLink", emailVerificationUrl)
        templateConfiguration!!.setClassForTemplateLoading(javaClass, basePackagePath)
        val template: Template = templateConfiguration!!.getTemplate("email-verification.ftl")
        val mailContent: String = FreeMarkerTemplateUtils.processTemplateIntoString(template, mail.getModel())
        mail.setContent(mailContent)
        send(mail)
    }

    /**
     * Setting the mail parameters.Send the reset link to the respective user's mail
     */
    @Throws(IOException::class, TemplateException::class, MessagingException::class)
    fun sendResetLink(resetPasswordLink: String, to: String) {
        val expirationInMinutes = TimeUnit.MILLISECONDS.toMinutes(expiration!!)
        val expirationInMinutesString = expirationInMinutes.toString()
        val mail = Mail()
        mail.setSubject("Password Reset Link [Team CEP]")
        mail.setTo(to)
        mail.setFrom(mailFrom!!)
        //TO DO
        //Color change by logo
        //mail.getModel().put("color", #HEX_VALUE_FROM_PROPERTIES)
        mail.getModel().put("path", path!!)
        mail.getModel().put("company",companyLogoName!!)
        mail.getModel().put("userName", to)
        mail.getModel().put("userResetPasswordLink", resetPasswordLink)
        mail.getModel().put("expirationTime", expirationInMinutesString)
        templateConfiguration!!.setClassForTemplateLoading(javaClass, basePackagePath)
        val template: Template = templateConfiguration!!.getTemplate("reset-link.ftl")
        val mailContent: String = FreeMarkerTemplateUtils.processTemplateIntoString(template, mail.getModel())
        mail.setContent(mailContent)
        send(mail)
    }

    /**
     * Send an email to the user indicating an account change event with the correct
     * status
     */
    @Throws(IOException::class, TemplateException::class, MessagingException::class)
    fun sendAccountChangeEmail(action: String?, actionStatus: String?, to: String?) {
        val mail = Mail()
        mail.setSubject("Account Status Change [Team CEP]")
        mail.setTo(to)
        mail.setFrom(mailFrom!!)
        //TO DO
        //Color change by logo
        //mail.getModel().put("color", #HEX_VALUE_FROM_PROPERTIES)
        mail.getModel().put("path", path!!)
        mail.getModel().put("company",companyLogoName!!)
        mail.getModel().put("userName", to!!)
        mail.getModel().put("action", action!!)
        mail.getModel().put("actionStatus", actionStatus!!)
        templateConfiguration!!.setClassForTemplateLoading(javaClass, basePackagePath)
        val template: Template = templateConfiguration!!.getTemplate("account-activity-change.ftl")
        val mailContent: String = FreeMarkerTemplateUtils.processTemplateIntoString(template, mail.getModel())
        mail.setContent(mailContent)
        send(mail)
    }

    /**
     * Sends a simple mail as a MIME Multipart message
     */
    @Throws(MessagingException::class)
    fun send(mail: Mail) {
        val message: MimeMessage = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                StandardCharsets.UTF_8.name())
        helper.setTo(mail.getTo()!!)
        helper.setText(mail.getContent()!!, true)
        helper.setSubject(mail.getSubject()!!)
        helper.setFrom(mail.getFrom()!!)
        mailSender.send(message)
    }
    //TODO REMOVE




    // NEW VERSION

    fun htmlDOM(html: String): String {
        var result = ""
        val doc = org.jsoup.Jsoup.parseBodyFragment(html)
        val uls = doc.getElementsByTag("ul")
        val data: MutableMap<String, String> = mutableMapOf()
        for (ul in uls) {
            val lis = ul.getElementsByTag("li")
            for (li in lis) {
                val bs = li.getElementsByTag("b")
                val brs = li.getElementsByTag("br")
                if (bs.size == 1) {
                    data[bs[0].text().trim()] = li.html().substringAfter("<br>").substringBefore("<br>").trim()
                }
                println(li.html())
                println("================")
            }
        }
        result = """
            $result
            ${org.jsoup.Jsoup.parse(html).html()}
            """.trimIndent()
        return result
    }

    fun prepareFromMightyForm(html: String): MutableMap<String, String> {
        val doc = org.jsoup.Jsoup.parseBodyFragment(html)
        val uls = doc.getElementsByTag("ul")
        val data: MutableMap<String, String> = mutableMapOf()
        for (ul in uls) {
            val lis = ul.getElementsByTag("li")
            for (li in lis) {
                val bs = li.getElementsByTag("b")
                val brs = li.getElementsByTag("br")
                if (bs.size == 1) {
                    data[bs[0].text().trim()] = li.html().substringAfter("<br>").substringBefore("<br>").trim()
                }
            }
        }
        return data
    }

    fun prepareFromMightyFormArray(html: String): ArrayList<String> {
        val doc = org.jsoup.Jsoup.parseBodyFragment(html)
        val uls = doc.getElementsByTag("ul")
        val data: ArrayList<String> = arrayListOf()
        for (ul in uls) {
            val lis = ul.getElementsByTag("li")
            for (li in lis) {
                val bs = li.getElementsByTag("b")
                val brs = li.getElementsByTag("br")
                if (bs.size == 1) {
                    data.add(li.html().substringAfter("<br>").substringBefore("<br>").trim())
                }
            }
        }
        return data
    }
}
