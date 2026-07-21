package me.bossm0n5t3r.readability4k.service

import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpTimeoutException
import java.time.Duration
import me.bossm0n5t3r.readability4k.config.ReadabilityProperties
import me.bossm0n5t3r.readability4k.exception.ReadabilityException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class ReadabilityHtmlFetcher(private val properties: ReadabilityProperties) {
    private val httpClient: HttpClient =
        HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(properties.http.connectTimeoutSeconds))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build()

    fun fetch(link: String): String {
        val uri = parseUri(link)
        val request =
            HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofSeconds(properties.http.requestTimeoutSeconds))
                .header("User-Agent", properties.http.userAgent)
                .GET()
                .build()

        val response =
            try {
                httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            } catch (e: HttpTimeoutException) {
                throw ReadabilityException("Request timed out: $link", HttpStatus.GATEWAY_TIMEOUT)
            } catch (e: IOException) {
                throw ReadabilityException(
                    "Failed to fetch URL: ${e.message}",
                    HttpStatus.BAD_GATEWAY,
                )
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                throw ReadabilityException(
                    "Request interrupted: $link",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                )
            }

        if (response.statusCode() >= 400) {
            throw ReadabilityException(
                "Failed to fetch URL: HTTP ${response.statusCode()}",
                HttpStatus.BAD_GATEWAY,
            )
        }

        return response.body()
    }

    private fun parseUri(link: String): URI {
        val uri =
            try {
                URI.create(link)
            } catch (e: IllegalArgumentException) {
                throw ReadabilityException("Invalid URL: $link", HttpStatus.BAD_REQUEST)
            }

        if (uri.scheme !in setOf("http", "https")) {
            throw ReadabilityException(
                "URL must use http or https scheme: $link",
                HttpStatus.BAD_REQUEST,
            )
        }

        return uri
    }
}
