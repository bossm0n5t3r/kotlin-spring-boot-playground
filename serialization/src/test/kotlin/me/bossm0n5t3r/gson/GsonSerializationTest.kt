package me.bossm0n5t3r.gson

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import me.bossm0n5t3r.dto.PersonDto
import me.bossm0n5t3r.dto.SerializationTestData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GsonSerializationTest {
    private val gson: Gson =
        GsonBuilder()
            .setPrettyPrinting()
            .create()

    @Test
    fun `Gson serialization and deserialization test`() {
        val person = SerializationTestData.person

        // Serialization
        val json = gson.toJson(person)
        println("Gson JSON: $json")

        // Deserialization
        val deserializedPerson = gson.fromJson(json, PersonDto::class.java)

        assertEquals(person, deserializedPerson)
    }

    @Test
    fun `Gson handles missing fields but not Kotlin default arguments directly`() {
        val json = SerializationTestData.PERSON_JSON

        val deserializedPerson = gson.fromJson(json, PersonDto::class.java)

        assertEquals("John", deserializedPerson.name)
        assertEquals(25, deserializedPerson.age)
        assertEquals(null, deserializedPerson.email)

        // Note: Gson might not respect Kotlin's default values if the field is missing in JSON
        // because it uses unsafe reflection to create the object.
        // In some cases, hobbies might be null if not initialized correctly by Gson.
        assertEquals(null, deserializedPerson.hobbies)
    }
}
