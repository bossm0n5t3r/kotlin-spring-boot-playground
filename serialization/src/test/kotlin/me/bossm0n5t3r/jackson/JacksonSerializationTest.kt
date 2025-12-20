package me.bossm0n5t3r.jackson

import me.bossm0n5t3r.dto.PersonDto
import me.bossm0n5t3r.dto.SerializationTestData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue

class JacksonSerializationTest {
    private val mapper = jacksonObjectMapper()

    @Test
    fun `Jackson serialization and deserialization test`() {
        val person = SerializationTestData.person

        // Serialization
        val json = mapper.writeValueAsString(person)
        println("Jackson JSON: $json")

        // Deserialization
        val deserializedPerson = mapper.readValue<PersonDto>(json)

        assertEquals(person, deserializedPerson)
    }

    @Test
    fun `Jackson handles null values and default arguments`() {
        val json = SerializationTestData.PERSON_JSON

        val deserializedPerson = mapper.readValue<PersonDto>(json)

        assertEquals(SerializationTestData.personWithDefaultValues, deserializedPerson)
    }
}
