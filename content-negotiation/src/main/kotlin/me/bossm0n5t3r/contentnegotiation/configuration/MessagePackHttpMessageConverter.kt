package me.bossm0n5t3r.contentnegotiation.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.msgpack.jackson.dataformat.MessagePackFactory
import org.springframework.http.HttpInputMessage
import org.springframework.http.HttpOutputMessage
import org.springframework.http.MediaType
import org.springframework.http.converter.AbstractHttpMessageConverter

class MessagePackHttpMessageConverter :
    AbstractHttpMessageConverter<Any>(
        MediaType.parseMediaType(Constants.MSGPACK_MEDIA_TYPE),
    ) {
    private val objectMapper =
        ObjectMapper(MessagePackFactory())
            .registerKotlinModule()

    override fun supports(clazz: Class<*>): Boolean = true

    override fun readInternal(
        clazz: Class<out Any>,
        inputMessage: HttpInputMessage,
    ): Any = objectMapper.readValue(inputMessage.body, clazz)

    override fun writeInternal(
        t: Any,
        outputMessage: HttpOutputMessage,
    ) {
        objectMapper.writeValue(outputMessage.body, t)
    }
}
