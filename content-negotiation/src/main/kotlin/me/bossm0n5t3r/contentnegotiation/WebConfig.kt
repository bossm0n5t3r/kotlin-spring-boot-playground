package me.bossm0n5t3r.contentnegotiation

import com.fasterxml.jackson.databind.ObjectMapper
import org.msgpack.jackson.dataformat.MessagePackFactory
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer {
    override fun extendMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        converters.add(0, ProtobufHttpMessageConverter())
        converters.add(1, MessagePackHttpMessageConverter())
    }
}

class MessagePackHttpMessageConverter :
    AbstractJackson2HttpMessageConverter(
        ObjectMapper(MessagePackFactory()),
        MediaType("application", "x-msgpack"),
    )
