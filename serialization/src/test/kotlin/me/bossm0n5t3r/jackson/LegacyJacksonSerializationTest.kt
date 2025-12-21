package me.bossm0n5t3r.jackson

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.bossm0n5t3r.dto.DateTimeDto
import me.bossm0n5t3r.dto.PersonDto
import me.bossm0n5t3r.dto.SerializationTestData
import me.bossm0n5t3r.dto.SerializationTestData.WEIRD_DTO_RIGHT_JSON
import me.bossm0n5t3r.dto.SerializationTestData.WEIRD_DTO_WRONG_JSON
import me.bossm0n5t3r.dto.SerializationTestData.weirdDto
import me.bossm0n5t3r.dto.WeirdDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertNotEquals

class LegacyJacksonSerializationTest {
    private val mapper =
        jacksonObjectMapper()
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

    @Test
    fun `Jackson handles weird DTO`() {
        val wrongSerialized = mapper.writeValueAsString(weirdDto)
        assertNotEquals(WEIRD_DTO_RIGHT_JSON, wrongSerialized)
        assertEquals(WEIRD_DTO_WRONG_JSON, wrongSerialized)

        val deserialized = mapper.readValue<WeirdDto>(WEIRD_DTO_RIGHT_JSON)
        assertEquals(weirdDto, deserialized)

        val rightObjectMapper =
            jacksonObjectMapper {
                enable(KotlinFeature.KotlinPropertyNameAsImplicitName)
            }.registerModule(JavaTimeModule())

        val rightSerialized = rightObjectMapper.writeValueAsString(weirdDto)
        assertEquals(WEIRD_DTO_RIGHT_JSON, rightSerialized)
        assertNotEquals(WEIRD_DTO_WRONG_JSON, rightSerialized)

        val anotherDeserialized = rightObjectMapper.readValue<WeirdDto>(WEIRD_DTO_RIGHT_JSON)
        assertEquals(weirdDto, anotherDeserialized)
    }
}
