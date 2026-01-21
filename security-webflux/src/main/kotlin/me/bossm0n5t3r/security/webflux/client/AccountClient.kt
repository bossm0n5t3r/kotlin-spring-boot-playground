package me.bossm0n5t3r.security.webflux.client

import me.bossm0n5t3r.security.webflux.dto.LoginRequest
import me.bossm0n5t3r.security.webflux.dto.RegisterRequest
import me.bossm0n5t3r.security.webflux.dto.TokenResponse
import me.bossm0n5t3r.security.webflux.dto.UpdateRoleRequest
import me.bossm0n5t3r.security.webflux.dto.UserAccountResponse
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.annotation.PatchExchange
import org.springframework.web.service.annotation.PostExchange
import reactor.core.publisher.Mono

@HttpExchange("/api/account")
interface AccountClient {
    @PostExchange("/register")
    fun register(
        @RequestBody request: RegisterRequest,
    ): Mono<UserAccountResponse>

    @PostExchange("/login")
    fun login(
        @RequestBody request: LoginRequest,
    ): Mono<TokenResponse>

    @GetExchange("/me")
    fun getMe(
        @RequestHeader(HttpHeaders.AUTHORIZATION) authHeader: String,
    ): Mono<UserAccountResponse>

    @PatchExchange("/role")
    fun updateRole(
        @RequestHeader(HttpHeaders.AUTHORIZATION) authHeader: String,
        @RequestBody request: UpdateRoleRequest,
    ): Mono<UserAccountResponse>
}
