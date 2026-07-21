package me.bossm0n5t3r.readability4k.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForEntity

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class Readability4kControllerTest {
    @LocalServerPort var port: Int = 0

    private val restTemplate = RestTemplate()

    @Test
    fun `invalid url returns 400 for bossm0n5t3r endpoint`() {
        val status = getStatus("http://localhost:$port/readability4k/bossm0n5t3r?link=not-a-url")
        assertThat(status.isSameCodeAs(HttpStatus.BAD_REQUEST)).isTrue
    }

    @Test
    fun `invalid url returns 400 for sermilion endpoint`() {
        val status = getStatus("http://localhost:$port/readability4k/sermilion?link=not-a-url")
        assertThat(status.isSameCodeAs(HttpStatus.BAD_REQUEST)).isTrue
    }

    private fun getStatus(url: String): org.springframework.http.HttpStatusCode {
        return try {
            restTemplate.getForEntity<String>(url).statusCode
        } catch (e: HttpClientErrorException) {
            e.statusCode
        }
    }
}
