package me.bossm0n5t3r.kotlinx

import kotlinx.serialization.json.Json
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
}
