package me.bossm0n5t3r.account

import kotlinx.coroutines.runBlocking
import me.bossm0n5t3r.account.enumeration.UserRole
import me.bossm0n5t3r.account.model.LoginRequest
import me.bossm0n5t3r.account.model.RegisterRequest
import me.bossm0n5t3r.account.model.TokenResponse
import me.bossm0n5t3r.account.model.UpdateRoleRequest
import me.bossm0n5t3r.account.model.UserAccountResponse
import me.bossm0n5t3r.account.repository.UserAccountRepository
import me.bossm0n5t3r.account.util.Constants.BEARER_PREFIX
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AccountIntegrationTest {
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var userAccountRepository: UserAccountRepository

    @BeforeEach
    fun setUp(
        @Autowired applicationContext: ApplicationContext,
    ) {
        webTestClient = WebTestClient.bindToApplicationContext(applicationContext).build()
        runBlocking {
            userAccountRepository.deleteAll()
        }
    }

    @Test
    fun `유저 등록 테스트`() {
        val registerRequest = createRegisterRequest()

        val userResponse = registerUser(registerRequest)

        assert(userResponse.username == registerRequest.username)
        assert(userResponse.role == UserRole.ANONYMOUS)
    }

    @Test
    fun `로그인 성공 테스트`() {
        val registerRequest = createRegisterRequest()
        registerUser(registerRequest)

        val loginRequest = LoginRequest(username = registerRequest.username, password = registerRequest.password)
        val tokenResponse = loginUser(loginRequest)

        assert(tokenResponse.token.isNotBlank())
    }

    @Test
    fun `로그인 실패 테스트 - 잘못된 비밀번호`() {
        val registerRequest = createRegisterRequest()
        registerUser(registerRequest)

        val wrongLoginRequest = LoginRequest(username = registerRequest.username, password = "wrongpassword")
        webTestClient
            .post()
            .uri("/api/account/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(wrongLoginRequest)
            .exchange()
            .expectStatus()
            .is5xxServerError
    }

    @Test
    fun `정보 조회 테스트`() {
        val registerRequest = createRegisterRequest()
        registerUser(registerRequest)
        val loginRequest = LoginRequest(username = registerRequest.username, password = registerRequest.password)
        val token = loginUser(loginRequest).token

        webTestClient
            .get()
            .uri("/api/account/me")
            .header(HttpHeaders.AUTHORIZATION, "$BEARER_PREFIX$token")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.username")
            .isEqualTo(registerRequest.username)
            .jsonPath("$.nickname")
            .isEqualTo(registerRequest.nickname)
            .jsonPath("$.role")
            .isEqualTo(UserRole.ANONYMOUS.name)
    }

    @Test
    fun `Role 업데이트 테스트`() {
        val registerRequest = createRegisterRequest()
        registerUser(registerRequest)
        val loginRequest = LoginRequest(username = registerRequest.username, password = registerRequest.password)
        val token = loginUser(loginRequest).token

        // Role 업데이트
        val updateRoleRequest = UpdateRoleRequest(role = UserRole.ADMIN)
        webTestClient
            .patch()
            .uri("/api/account/role")
            .header(HttpHeaders.AUTHORIZATION, "$BEARER_PREFIX$token")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(updateRoleRequest)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.role")
            .isEqualTo(UserRole.ADMIN.name)

        // 업데이트 후 정보 조회 확인
        webTestClient
            .get()
            .uri("/api/account/me")
            .header(HttpHeaders.AUTHORIZATION, "$BEARER_PREFIX$token")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.role")
            .isEqualTo(UserRole.ADMIN.name)
    }

    private fun createRegisterRequest() =
        RegisterRequest(
            username = "testuser",
            nickname = "테스터",
            email = "test@example.com",
            password = "testpassword",
        )

    private fun registerUser(registerRequest: RegisterRequest): UserAccountResponse =
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
            .responseBody
            .let { requireNotNull(it) { "UserAccountResponse is null" } }

    private fun loginUser(loginRequest: LoginRequest): TokenResponse =
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
            .responseBody
            .let { requireNotNull(it) { "Token is null" } }
}
