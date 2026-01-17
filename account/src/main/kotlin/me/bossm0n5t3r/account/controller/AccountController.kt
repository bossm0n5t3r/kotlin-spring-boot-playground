package me.bossm0n5t3r.account.controller

import me.bossm0n5t3r.account.model.RegisterRequest
import me.bossm0n5t3r.account.model.TokenResponse
import me.bossm0n5t3r.account.model.UserAccountResponse
import me.bossm0n5t3r.account.service.AccountService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/account")
class AccountController(
    private val accountService: AccountService,
) {
    @PostMapping("/register")
    suspend fun register(
        @RequestBody request: RegisterRequest,
    ): UserAccountResponse = accountService.register(request)

    @GetMapping("/token")
    suspend fun getToken(
        @RequestParam username: String,
    ): TokenResponse = accountService.getToken(username)

    @GetMapping("/me")
    suspend fun getMe(
        @RequestHeader("Authorization") authHeader: String,
    ): UserAccountResponse {
        val token = authHeader.removePrefix("Bearer ").trim()
        return accountService.getUserInfo(token)
    }
}
