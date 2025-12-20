package me.bossm0n5t3r.dto

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
}
