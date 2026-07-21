package me.bossm0n5t3r.readability4k.controller

import jakarta.validation.constraints.Pattern
import me.bossm0n5t3r.readability4k.domain.ReadabilityLibrary
import me.bossm0n5t3r.readability4k.dto.ReadabilityData
import me.bossm0n5t3r.readability4k.service.Readability4kService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/readability4k")
@Validated
class Readability4kController(private val readability4kService: Readability4kService) {
    @GetMapping("/{library}")
    fun extract(
        @PathVariable library: String,
        @RequestParam
        @Pattern(regexp = "^https?://.*", message = "URL must start with http:// or https://")
        link: String,
    ): ReadabilityData {
        val readabilityLibrary = ReadabilityLibrary.from(library)
        return readability4kService.extract(readabilityLibrary, link)
    }
}
