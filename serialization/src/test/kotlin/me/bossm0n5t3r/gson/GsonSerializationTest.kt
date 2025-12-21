package me.bossm0n5t3r.gson

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import me.bossm0n5t3r.dto.DateTimeDto
import me.bossm0n5t3r.dto.OFFSET_DATE_TIME_PATTERN
import me.bossm0n5t3r.dto.PersonDto
import me.bossm0n5t3r.dto.SerializationTestData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class GsonSerializationTest {
    private val gson: Gson =
        GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
            .registerTypeAdapter(OffsetDateTime::class.java, OffsetDateTimeAdapter())
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

    @Test
    fun `Gson serialization and deserialization test for LocalDateTime`() {
        val dateTimeDto = SerializationTestData.dateTimeDto

        // Serialization
        val json = gson.toJson(dateTimeDto)
        println("Gson DateTime JSON: $json")

        // Deserialization
        val deserializedDateTimeDto = gson.fromJson(json, DateTimeDto::class.java)

        assertEquals(dateTimeDto, deserializedDateTimeDto)
    }

    @Test
    fun `Gson handles DATE_TIME_JSON`() {
        val json = SerializationTestData.DATE_TIME_JSON

        val deserializedDateTimeDto = gson.fromJson(json, DateTimeDto::class.java)

        assertEquals(SerializationTestData.dateTimeDto, deserializedDateTimeDto)
    }

    private class LocalDateTimeAdapter :
        JsonSerializer<LocalDateTime>,
        JsonDeserializer<LocalDateTime> {
        private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

        override fun serialize(
            src: LocalDateTime,
            typeOfSrc: Type,
            context: JsonSerializationContext,
        ): JsonElement = JsonPrimitive(formatter.format(src))

        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext,
        ): LocalDateTime = LocalDateTime.parse(json.asString, formatter)
    }

    private class OffsetDateTimeAdapter :
        JsonSerializer<OffsetDateTime>,
        JsonDeserializer<OffsetDateTime> {
        private val formatter = DateTimeFormatter.ofPattern(OFFSET_DATE_TIME_PATTERN)

        override fun serialize(
            src: OffsetDateTime,
            typeOfSrc: Type,
            context: JsonSerializationContext,
        ): JsonElement = JsonPrimitive(formatter.format(src))

        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext,
        ): OffsetDateTime = OffsetDateTime.parse(json.asString, formatter)
    }
}
