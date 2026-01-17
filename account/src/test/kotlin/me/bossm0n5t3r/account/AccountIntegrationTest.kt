package me.bossm0n5t3r.account

import me.bossm0n5t3r.account.model.RegisterRequest
import me.bossm0n5t3r.account.model.TokenResponse
import me.bossm0n5t3r.account.model.UserAccountResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AccountIntegrationTest {
    private lateinit var webTestClient: WebTestClient

    @BeforeEach
    fun setUp(
        @Autowired applicationContext: ApplicationContext,
    ) {
        webTestClient = WebTestClient.bindToApplicationContext(applicationContext).build()
    }

    @Test
    fun `유저 등록, 토큰 발급 및 정보 조회 시나리오`() {
        val registerRequest =
            RegisterRequest(
                username = "testuser",
                nickname = "테스터",
                email = "test@example.com",
            )

        // 1. 유저 등록
        val userResponse =
            webTestClient
                .post()
                .uri("/api/account/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(registerRequest)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<UserAccountResponse>()
                .returnResult()
                .responseBody!!

        assert(userResponse.username == "testuser")

        // 2. 토큰 발급
        val tokenResponse =
            webTestClient
                .get()
                .uri { it.path("/api/account/token").queryParam("username", "testuser").build() }
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<TokenResponse>()
                .returnResult()
                .responseBody!!

        val token = tokenResponse.token
        assert(token.isNotBlank())

        // 3. 정보 조회
        webTestClient
            .get()
            .uri("/api/account/me")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.username")
            .isEqualTo("testuser")
            .jsonPath("$.nickname")
            .isEqualTo("테스터")
    }
}
