package me.bossm0n5t3r.account.service

import me.bossm0n5t3r.account.domain.UserAccount
import me.bossm0n5t3r.account.model.RegisterRequest
import me.bossm0n5t3r.account.model.TokenResponse
import me.bossm0n5t3r.account.model.UserAccountResponse
import me.bossm0n5t3r.account.repository.UserAccountRepository
import me.bossm0n5t3r.account.util.JwtProvider
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AccountService(
    private val userAccountRepository: UserAccountRepository,
    private val jwtProvider: JwtProvider,
) {
    @Transactional
    suspend fun register(request: RegisterRequest): UserAccountResponse {
        val userAccount =
            UserAccount(
                username = request.username,
                nickname = request.nickname,
                email = request.email,
            )
        return userAccountRepository
            .save(userAccount)
            .let { saved ->
                UserAccountResponse(
                    id = saved.id,
                    username = saved.username,
                    nickname = saved.nickname,
                    email = saved.email,
                )
            }
    }

    suspend fun getToken(username: String): TokenResponse =
        userAccountRepository
            .findByUsername(username)
            .let { TokenResponse(jwtProvider.createToken(it.username)) }

    suspend fun getUserInfo(token: String): UserAccountResponse {
        require(jwtProvider.validateToken(token)) { "Invalid token" }
        val username = jwtProvider.getUsernameFromToken(token)
        return userAccountRepository
            .findByUsername(username)
            .let {
                UserAccountResponse(
                    id = it.id,
                    username = it.username,
                    nickname = it.nickname,
                    email = it.email,
                )
            }
    }
}
