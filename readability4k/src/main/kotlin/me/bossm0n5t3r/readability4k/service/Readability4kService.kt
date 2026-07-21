package me.bossm0n5t3r.readability4k.service

import me.bossm0n5t3r.readability4k.domain.ReadabilityLibrary
import me.bossm0n5t3r.readability4k.dto.ReadabilityData
import me.bossm0n5t3r.readability4k.exception.ReadabilityException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class Readability4kService(extractors: List<ReadabilityExtractor>) {
    private val extractorMap = extractors.associateBy { it.library }

    fun extract(library: ReadabilityLibrary, link: String): ReadabilityData {
        val extractor =
            extractorMap[library]
                ?: throw ReadabilityException(
                    "Unsupported library: ${library.key}",
                    HttpStatus.BAD_REQUEST,
                )
        return extractor.extract(link)
    }
}
