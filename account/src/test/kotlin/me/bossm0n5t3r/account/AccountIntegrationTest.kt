package me.bossm0n5t3r.account

import me.bossm0n5t3r.account.enumeration.UserRole
import me.bossm0n5t3r.account.model.LoginRequest
import me.bossm0n5t3r.account.model.RegisterRequest
import me.bossm0n5t3r.account.model.TokenResponse
import me.bossm0n5t3r.account.model.UpdateRoleRequest
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
                password = "testpassword",
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
        assert(userResponse.role == UserRole.ANONYMOUS)

        // 2. 로그인 (성공)
        val loginRequest = LoginRequest(username = "testuser", password = "testpassword")
        val tokenResponse =
            webTestClient
                .post()
                .uri("/api/account/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<TokenResponse>()
                .returnResult()
                .responseBody!!

        val token = tokenResponse.token
        assert(token.isNotBlank())

        // 2-1. 로그인 (실패 - 잘못된 비밀번호)
        val wrongLoginRequest = LoginRequest(username = "testuser", password = "wrongpassword")
        webTestClient
            .post()
            .uri("/api/account/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(wrongLoginRequest)
            .exchange()
            .expectStatus()
            .is5xxServerError // require() 실패 시 500 에러 발생 (기본 설정 시)

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
            .jsonPath("$.role")
            .isEqualTo("ANONYMOUS")

        // 4. Role 업데이트
        val updateRoleRequest = UpdateRoleRequest(role = UserRole.ADMIN)
        webTestClient
            .patch()
            .uri("/api/account/role")
            .header("Authorization", "Bearer $token")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(updateRoleRequest)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.role")
            .isEqualTo("ADMIN")

        // 5. 업데이트 후 정보 조회 확인
        webTestClient
            .get()
            .uri("/api/account/me")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.role")
            .isEqualTo("ADMIN")
    }
}
