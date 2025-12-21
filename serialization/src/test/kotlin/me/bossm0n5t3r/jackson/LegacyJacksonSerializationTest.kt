package me.bossm0n5t3r.jackson

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.readValue
import me.bossm0n5t3r.dto.DateTimeDto
import me.bossm0n5t3r.dto.PersonDto
import me.bossm0n5t3r.dto.SerializationTestData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertNotEquals
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper as legacyJacksonObjectMapper

class LegacyJacksonSerializationTest {
    private val mapper =
        legacyJacksonObjectMapper()
            .registerModule(JavaTimeModule())

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

    @Test
    fun `Jackson serialization and deserialization test for LocalDateTime`() {
        val dateTimeDto = SerializationTestData.dateTimeDto

        // Serialization
        val json = mapper.writeValueAsString(dateTimeDto)
        println("Jackson DateTime JSON: $json")

        // Deserialization
        val deserializedDateTimeDto = mapper.readValue<DateTimeDto>(json)

        assertEquals(dateTimeDto.name, deserializedDateTimeDto.name)
        assertEquals(dateTimeDto.createdAt, deserializedDateTimeDto.createdAt)
        assertEquals(dateTimeDto.updatedAt.toInstant(), deserializedDateTimeDto.updatedAt.toInstant())
    }

    @Test
    fun `Jackson handles DATE_TIME_JSON`() {
        val json = SerializationTestData.DATE_TIME_JSON

        val deserializedDateTimeDto = mapper.readValue<DateTimeDto>(json)

        assertEquals(SerializationTestData.dateTimeDto.name, deserializedDateTimeDto.name)
        assertEquals(SerializationTestData.dateTimeDto.createdAt, deserializedDateTimeDto.createdAt)
        assertEquals(SerializationTestData.dateTimeDto.updatedAt.toInstant(), deserializedDateTimeDto.updatedAt.toInstant())
    }

    private data class WeirdDto(
        val xId: String,
    )

    @Test
    fun `Jackson handles weird DTO`() {
        val dto = WeirdDto("It’s really weird.")
        val weirdJson = """{"xId":"It’s really weird."}"""

        val serialized = mapper.writeValueAsString(dto)
        assertNotEquals(weirdJson, serialized)

        val deserialized = mapper.readValue<WeirdDto>(weirdJson)
        assertEquals(dto, deserialized)

        val rightObjectMapper =
            legacyJacksonObjectMapper {
                enable(KotlinFeature.KotlinPropertyNameAsImplicitName)
            }.registerModule(JavaTimeModule())

        val rightSerialized = rightObjectMapper.writeValueAsString(dto)
        assertEquals(weirdJson, rightSerialized)

        val rightDeserialized = rightObjectMapper.readValue<WeirdDto>(weirdJson)
        assertEquals(dto, rightDeserialized)
    }
}
