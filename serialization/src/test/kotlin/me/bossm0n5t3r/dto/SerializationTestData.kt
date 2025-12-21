package me.bossm0n5t3r.dto

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

object SerializationTestData {
    val person =
        PersonDto(
            name = "bossm0n5t3r",
            age = 31,
            email = "bossm0n5t3r@example.com",
            hobbies = listOf("Coding", "Reading"),
        )

    const val PERSON_JSON = """{"name":"John","age":25}"""

    val personWithDefaultValues =
        PersonDto(
            name = "John",
            age = 25,
            email = null,
            hobbies = emptyList(),
        )

    val dateTimeDto =
        DateTimeDto(
            name = "bossm0n5t3r",
            createdAt = LocalDateTime.of(2006, 1, 2, 15, 4, 5),
            updatedAt = OffsetDateTime.of(2006, 1, 2, 15, 4, 5, 0, ZoneOffset.ofHours(-7)),
        )

    const val DATE_TIME_JSON =
        """{"name":"bossm0n5t3r","createdAt":"2006-01-02T15:04:05","updatedAt":"01/02 03:04:05PM '06 -0700"}"""

    val weirdDto =
        WeirdDto(
            xId = "bossm0n5t3r",
            xMessage = "It’s really weird.",
            xxName = "First Name, Last Name",
            xxxAge = 31,
        )
    const val WEIRD_DTO_RIGHT_JSON =
        """{"xId":"bossm0n5t3r","xMessage":"It’s really weird.","xxName":"First Name, Last Name","xxxAge":31}"""
    const val WEIRD_DTO_WRONG_JSON =
        """{"xxName":"First Name, Last Name","xxxAge":31,"xid":"bossm0n5t3r","xmessage":"It’s really weird."}"""
}
