package me.bossm0n5t3r.account.controller

import me.bossm0n5t3r.account.domain.UserAccount
import me.bossm0n5t3r.account.model.LoginRequest
import me.bossm0n5t3r.account.model.RegisterRequest
import me.bossm0n5t3r.account.model.TokenResponse
import me.bossm0n5t3r.account.model.UpdateRoleRequest
import me.bossm0n5t3r.account.model.UserAccountResponse
import me.bossm0n5t3r.account.service.AccountService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
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

    @PostMapping("/login")
    suspend fun login(
        @RequestBody request: LoginRequest,
    ): TokenResponse = accountService.login(request)

    @GetMapping("/me")
    suspend fun getMe(
        @AuthenticationPrincipal userAccount: UserAccount,
    ): UserAccountResponse =
        UserAccountResponse(
            id = userAccount.id,
            username = userAccount.username,
            nickname = userAccount.nickname,
            email = userAccount.email,
            role = userAccount.role,
        )

    @PatchMapping("/role")
    suspend fun updateRole(
        @AuthenticationPrincipal userAccount: UserAccount,
        @RequestBody request: UpdateRoleRequest,
    ): UserAccountResponse = accountService.updateRole(userAccount.username, request)
}
