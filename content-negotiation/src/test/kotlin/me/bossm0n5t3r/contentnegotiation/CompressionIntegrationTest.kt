package me.bossm0n5t3r.contentnegotiation

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.core5.http.io.entity.EntityUtils
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.zip.GZIPInputStream

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CompressionIntegrationTest {
    @LocalServerPort
    private var port: Int = 0

    private val objectMapper = ObjectMapper().registerKotlinModule()

    private val results = mutableListOf<TestResult>()

    data class TestResult(
        val format: String,
        val endpoint: String,
        val acceptHeader: String,
        val identityBytes: Long,
        val gzipBytes: Long,
        val ratio: Double?,
        val gzipApplied: Boolean,
    )

    @Test
    fun testCompression() {
        val formats =
            listOf(
                "json" to "application/json",
                "msgpack" to Constants.MSGPACK_MEDIA_TYPE,
                "protobuf" to Constants.PROTOBUF_MEDIA_TYPE,
            )
        val endpoints = listOf("/payload/small", "/payload/medium", "/payload/large")

        // Use custom HttpClient that doesn't auto-decompress
        HttpClients.custom().disableContentCompression().build().use { client ->
            for ((formatName, accept) in formats) {
                for (endpoint in endpoints) {
                    measureAndVerify(client, formatName, accept, endpoint)
                }
            }
        }
    }

    private fun measureAndVerify(
        client: CloseableHttpClient,
        format: String,
        accept: String,
        endpoint: String,
    ) {
        val url = "http://localhost:$port$endpoint"

        // 1. Identity
        val identityRequest =
            HttpGet(url).apply {
                addHeader(Constants.ACCEPT_HEADER, accept)
                addHeader(Constants.ACCEPT_ENCODING_HEADER, Constants.IDENTITY_ENCODING)
            }
        val identityBytes =
            client.execute(identityRequest) { response ->
                EntityUtils.toByteArray(response.entity)
            }

        // 2. Gzip
        val gzipRequest =
            HttpGet(url).apply {
                addHeader(Constants.ACCEPT_HEADER, accept)
                addHeader(Constants.ACCEPT_ENCODING_HEADER, Constants.GZIP_ENCODING)
            }
        val (gzipBytes, gzipApplied) =
            client.execute(gzipRequest) { response ->
                val encoding = response.getHeader(Constants.CONTENT_ENCODING_HEADER)?.value
                val bytes = EntityUtils.toByteArray(response.entity)
                bytes to (encoding == Constants.GZIP_ENCODING)
            }

        val ratio =
            if (gzipApplied) {
                String.format("%.3f", gzipBytes.size.toDouble() / identityBytes.size).toDouble()
            } else {
                null
            }

        // Verify decompressed content matches identity
        if (gzipApplied) {
            val decompressedBytes = GZIPInputStream(gzipBytes.inputStream()).use { it.readAllBytes() }
            assertArrayEquals(identityBytes, decompressedBytes, "Decompressed bytes for $format $endpoint should match identity bytes")
        } else {
            assertArrayEquals(
                identityBytes,
                gzipBytes,
                "Non-compressed gzip request bytes for $format $endpoint should match identity bytes",
            )
        }

        results.add(
            TestResult(
                format = format,
                endpoint = endpoint,
                acceptHeader = accept,
                identityBytes = identityBytes.size.toLong(),
                gzipBytes = gzipBytes.size.toLong(),
                ratio = ratio,
                gzipApplied = gzipApplied,
            ),
        )
    }

    @AfterAll
    fun tearDown() {
        printMarkdownTable()
        saveJsonResponse()
    }

    private fun printMarkdownTable() {
        println("\n### Compression Test Report")
        println("| Format | Endpoint | Identity (B) | Gzip (B) | Ratio | GzipApplied |")
        println("| :--- | :--- | :--- | :--- | :--- | :--- |")
        results.sortedWith(compareBy({ it.format }, { it.endpoint })).forEach {
            val ratioStr = it.ratio?.toString() ?: "-"
            println("| ${it.format} | ${it.endpoint} | ${it.identityBytes} | ${it.gzipBytes} | $ratioStr | ${it.gzipApplied} |")
        }
    }

    private fun saveJsonResponse() {
        val reportDir = "build/reports/content-negotiation"
        Files.createDirectories(Paths.get(reportDir))
        val reportFile = File("$reportDir/compression-report.json")

        val report =
            mapOf(
                "generatedAt" to OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                "cases" to results,
            )

        val mapper = ObjectMapper().registerKotlinModule()
        reportFile.writeText(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(report))
        println("\nJSON report saved to: ${reportFile.absolutePath}")
    }
}
