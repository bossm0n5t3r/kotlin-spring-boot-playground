package me.bossm0n5t3r.account.security

import me.bossm0n5t3r.account.enumeration.UserRole
import me.bossm0n5t3r.account.model.LoginRequest
import me.bossm0n5t3r.account.model.RegisterRequest
import me.bossm0n5t3r.account.model.TokenResponse
import me.bossm0n5t3r.account.model.UpdateRoleRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpHeaders
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BearerTokenSecurityTest {
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    private var userToken: String? = null
    private var adminToken: String? = null

    @BeforeEach
    fun setUp() {
        webTestClient =
            WebTestClient
                .bindToApplicationContext(applicationContext)
                .configureClient()
                .build()

        val user = RegisterRequest("user", "user_nick", "user@test.com", "password", UserRole.USER)
        val admin = RegisterRequest("admin", "admin_nick", "admin@test.com", "password", UserRole.ADMIN)

        registerAndLogin(user)
        registerAndLogin(admin)
    }

    private fun registerAndLogin(request: RegisterRequest) {
        webTestClient
            .post()
            .uri("/api/account/register")
            .bodyValue(request)
            .exchange()

        val tokenResponse =
            webTestClient
                .post()
                .uri("/api/account/login")
                .bodyValue(LoginRequest(request.username, request.password))
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<TokenResponse>()
                .returnResult()
                .responseBody

        if (request.username == "user") userToken = tokenResponse?.token
        if (request.username == "admin") adminToken = tokenResponse?.token
    }

    @Test
    fun `401 - Authorization header missing`() {
        webTestClient
            .get()
            .uri("/api/account/me")
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    fun `400 - Authorization header invalid format`() {
        webTestClient
            .get()
            .uri("/api/account/me")
            .header(HttpHeaders.AUTHORIZATION, "Basic abc")
            .exchange()
            .expectStatus()
            .isBadRequest
    }

    @Test
    fun `400 - Token contains whitespace`() {
        webTestClient
            .get()
            .uri("/api/account/me")
            .header(HttpHeaders.AUTHORIZATION, "Bearer invalid token with space")
            .exchange()
            .expectStatus()
            .isBadRequest
    }

    @Test
    fun `401 - Invalid token signature`() {
        webTestClient
            .get()
            .uri("/api/account/me")
            .header(HttpHeaders.AUTHORIZATION, "Bearer invalid.token.here")
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    fun `403 - Role insufficient`() {
        webTestClient
            .patch()
            .uri("/api/account/role")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $userToken")
            .bodyValue(UpdateRoleRequest(UserRole.ADMIN))
            .exchange()
            .expectStatus()
            .isForbidden
    }

    @Test
    fun `200 - Successful authentication with ADMIN role`() {
        webTestClient
            .patch()
            .uri("/api/account/role")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $adminToken")
            .bodyValue(UpdateRoleRequest(UserRole.PREMIUM))
            .exchange()
            .expectStatus()
            .isOk
    }

    @Test
    fun `200 - Successful authentication`() {
        webTestClient
            .get()
            .uri("/api/account/me")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $userToken")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.username")
            .isEqualTo("user")
    }
}
