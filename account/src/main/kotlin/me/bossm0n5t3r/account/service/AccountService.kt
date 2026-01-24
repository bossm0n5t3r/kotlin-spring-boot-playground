package me.bossm0n5t3r.account.service

import me.bossm0n5t3r.account.domain.UserAccount
import me.bossm0n5t3r.account.enumeration.UserRole
import me.bossm0n5t3r.account.model.LoginRequest
import me.bossm0n5t3r.account.model.RegisterRequest
import me.bossm0n5t3r.account.model.TokenResponse
import me.bossm0n5t3r.account.model.UpdateRoleRequest
import me.bossm0n5t3r.account.model.UserAccountResponse
import me.bossm0n5t3r.account.repository.UserAccountRepository
import me.bossm0n5t3r.account.util.JwtProvider
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AccountService(
    private val userAccountRepository: UserAccountRepository,
    private val jwtProvider: JwtProvider,
    private val passwordEncoder: PasswordEncoder,
) {
    @Transactional
    suspend fun register(request: RegisterRequest): UserAccountResponse {
        val encodedPassword = passwordEncoder.encode(request.password)
        requireNotNull(encodedPassword) { "Failed to encode password" }
        val userAccount =
            UserAccount(
                username = request.username,
                nickname = request.nickname,
                email = request.email,
                password = encodedPassword,
                role = UserRole.ANONYMOUS,
            )
        return userAccountRepository
            .save(userAccount)
            .let { saved ->
                UserAccountResponse(
                    id = saved.id,
                    username = saved.username,
                    nickname = saved.nickname,
                    email = saved.email,
                    role = saved.role,
                )
            }
    }

    @Transactional
    suspend fun updateRole(
        username: String,
        request: UpdateRoleRequest,
    ): UserAccountResponse {
        val userAccount = userAccountRepository.findByUsername(username)
        val updatedUserAccount = userAccount.copy(role = request.role)
        return userAccountRepository
            .save(updatedUserAccount)
            .let { saved ->
                UserAccountResponse(
                    id = saved.id,
                    username = saved.username,
                    nickname = saved.nickname,
                    email = saved.email,
                    role = saved.role,
                )
            }
    }

    suspend fun login(request: LoginRequest): TokenResponse {
        val userAccount = userAccountRepository.findByUsername(request.username)
        require(passwordEncoder.matches(request.password, userAccount.password)) { "Invalid password" }
        return TokenResponse(jwtProvider.createToken(userAccount.username))
    }
}
