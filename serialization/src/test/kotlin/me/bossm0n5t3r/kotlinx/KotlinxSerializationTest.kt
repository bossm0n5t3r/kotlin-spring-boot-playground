package me.bossm0n5t3r.kotlinx

import kotlinx.serialization.json.Json
import me.bossm0n5t3r.dto.DateTimeDto
import me.bossm0n5t3r.dto.PersonDto
import me.bossm0n5t3r.dto.SerializationTestData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class KotlinxSerializationTest {
    private val json =
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            encodeDefaults = true
        }

    @Test
    fun `kotlinx-serialization serialization and deserialization test`() {
        val person = SerializationTestData.person

        // Serialization
        val jsonString = json.encodeToString(person)
        println("kotlinx-serialization JSON: $jsonString")

        // Deserialization
        val deserializedPerson = json.decodeFromString<PersonDto>(jsonString)

        assertEquals(person, deserializedPerson)
    }

    @Test
    fun `kotlinx-serialization handles default arguments`() {
        val jsonString = SerializationTestData.PERSON_JSON

        val deserializedPerson = json.decodeFromString<PersonDto>(jsonString)

        assertEquals(SerializationTestData.personWithDefaultValues, deserializedPerson)
    }

    @Test
    fun `kotlinx-serialization serialization and deserialization test for LocalDateTime`() {
        val dateTimeDto = SerializationTestData.dateTimeDto

        // Serialization
        val jsonString = json.encodeToString(dateTimeDto)
        println("kotlinx-serialization DateTime JSON: $jsonString")

        // Deserialization
        val deserializedDateTimeDto = json.decodeFromString<DateTimeDto>(jsonString)

        assertEquals(dateTimeDto, deserializedDateTimeDto)
    }

    @Test
    fun `kotlinx-serialization handles DATE_TIME_JSON`() {
        val jsonString = SerializationTestData.DATE_TIME_JSON

        val deserializedDateTimeDto = json.decodeFromString<DateTimeDto>(jsonString)

        assertEquals(SerializationTestData.dateTimeDto, deserializedDateTimeDto)
    }
}
