package com.mdsp.backend.app.system.config

import feign.codec.Decoder
import feign.codec.Encoder
import feign.form.spring.SpringFormEncoder
import feign.optionals.OptionalDecoder
import org.springframework.beans.factory.ObjectFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.http.HttpMessageConverters
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder
import org.springframework.cloud.openfeign.support.SpringDecoder
import org.springframework.cloud.openfeign.support.SpringEncoder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Scope
import org.springframework.web.client.RestTemplate


@Configuration
class FeignSupportConfig {

    @Autowired
    private val messageConverters: ObjectFactory<HttpMessageConverters>? = null

    @Bean
    @Primary
    @Scope("prototype")
    fun multipartFormEncoder(): Encoder {
        return SpringFormEncoder(SpringEncoder {
            HttpMessageConverters(RestTemplate().messageConverters)
        })
    }

    @Bean
//    @ConditionalOnMissingBean
    @Primary
    @Scope("prototype")
    fun feignDecoder(): Decoder? {
        return OptionalDecoder(
            ResponseEntityDecoder(SpringDecoder(messageConverters))
        )
    }

//    @Bean
//    @Primary
//    @Scope("prototype")
//    fun feignDecoder(): Decoder {
//        val springConverters = messageConverters!!.getObject().converters
//        val decoderConverters: MutableList<HttpMessageConverter<*>> = ArrayList(springConverters.size + 1)
//        decoderConverters.addAll(springConverters)
//        decoderConverters.add(SpringManyMultipartFilesReader(4096))
//        val httpMessageConverters = HttpMessageConverters(decoderConverters)
//        return SpringDecoder { httpMessageConverters }
//    }
}
