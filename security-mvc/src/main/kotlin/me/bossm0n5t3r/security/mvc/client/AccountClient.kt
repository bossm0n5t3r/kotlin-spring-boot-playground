package me.bossm0n5t3r.security.mvc.client

import me.bossm0n5t3r.security.mvc.dto.LoginRequest
import me.bossm0n5t3r.security.mvc.dto.RegisterRequest
import me.bossm0n5t3r.security.mvc.dto.TokenResponse
import me.bossm0n5t3r.security.mvc.dto.UpdateRoleRequest
import me.bossm0n5t3r.security.mvc.dto.UserAccountResponse
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.annotation.PatchExchange
import org.springframework.web.service.annotation.PostExchange

@HttpExchange("/api/account")
interface AccountClient {
    @PostExchange("/register")
    fun register(
        @RequestBody request: RegisterRequest,
    ): UserAccountResponse

    @PostExchange("/login")
    fun login(
        @RequestBody request: LoginRequest,
    ): TokenResponse

    @GetExchange("/me")
    fun getMe(
        @RequestHeader(HttpHeaders.AUTHORIZATION) authHeader: String,
    ): UserAccountResponse

    @PatchExchange("/role")
    fun updateRole(
        @RequestHeader(HttpHeaders.AUTHORIZATION) authHeader: String,
        @RequestBody request: UpdateRoleRequest,
    ): UserAccountResponse
}
