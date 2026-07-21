package me.bossm0n5t3r.readability4k.service

import com.sermilion.readability4k.Readability4K
import me.bossm0n5t3r.readability4k.domain.ReadabilityLibrary
import me.bossm0n5t3r.readability4k.dto.ReadabilityData
import org.springframework.stereotype.Component

@Component
class SermilionReadabilityExtractor(private val htmlFetcher: ReadabilityHtmlFetcher) :
    ReadabilityExtractor {
    override val library: ReadabilityLibrary = ReadabilityLibrary.SERMILION

    override fun extract(link: String): ReadabilityData {
        val html = htmlFetcher.fetch(link)
        val readability = Readability4K(link, html)
        val article = readability.parse()

        return ReadabilityData(
            title = article.title,
            author = article.byline,
            content = article.content,
            textContent = article.textContent,
        )
    }
}
