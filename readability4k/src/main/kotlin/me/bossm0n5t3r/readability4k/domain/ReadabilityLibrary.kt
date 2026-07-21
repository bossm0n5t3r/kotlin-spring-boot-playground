package me.bossm0n5t3r.readability4k.domain

enum class ReadabilityLibrary(val key: String) {
    BOSSM0N5T3R("bossm0n5t3r"),
    SERMILION("sermilion");

    companion object {
        fun from(key: String): ReadabilityLibrary {
            val normalized = key.lowercase()
            return entries.find { it.key == normalized }
                ?: throw IllegalArgumentException("Unsupported library: $key")
        }
    }
}
