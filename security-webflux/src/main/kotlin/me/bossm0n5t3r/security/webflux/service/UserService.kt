package me.bossm0n5t3r.security.webflux.service

import me.bossm0n5t3r.security.webflux.context.ReactiveUserContext
import me.bossm0n5t3r.security.webflux.dto.UserDetail
import org.springframework.stereotype.Service

@Service
class UserService {
    suspend fun getCurrentUser(): UserDetail? = ReactiveUserContext.currentUserOrNull()
}
