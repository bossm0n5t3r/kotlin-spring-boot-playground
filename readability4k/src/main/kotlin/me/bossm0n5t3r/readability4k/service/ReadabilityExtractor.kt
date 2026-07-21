package me.bossm0n5t3r.readability4k.service

import me.bossm0n5t3r.readability4k.domain.ReadabilityLibrary
import me.bossm0n5t3r.readability4k.dto.ReadabilityData

interface ReadabilityExtractor {
    val library: ReadabilityLibrary

    fun extract(link: String): ReadabilityData
}
