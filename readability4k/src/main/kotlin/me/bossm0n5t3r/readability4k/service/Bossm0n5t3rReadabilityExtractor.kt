package me.bossm0n5t3r.readability4k.service

import com.m0n5t3r.boss.readability4k.Readability
import com.m0n5t3r.boss.readability4k.dom.DOMParser
import me.bossm0n5t3r.readability4k.domain.ReadabilityLibrary
import me.bossm0n5t3r.readability4k.dto.ReadabilityData
import org.springframework.stereotype.Component

@Component
class Bossm0n5t3rReadabilityExtractor(private val htmlFetcher: ReadabilityHtmlFetcher) :
    ReadabilityExtractor {
    override val library: ReadabilityLibrary = ReadabilityLibrary.BOSSM0N5T3R

    override fun extract(link: String): ReadabilityData {
        val html = htmlFetcher.fetch(link)
        val document = DOMParser().parse(html, link)
        val article = Readability(document).parse()
        return ReadabilityData(
            title = article?.title,
            author = article?.byline,
            content = article?.content,
            textContent = article?.textContent,
        )
    }
}
