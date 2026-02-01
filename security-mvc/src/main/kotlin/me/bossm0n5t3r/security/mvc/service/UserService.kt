package me.bossm0n5t3r.security.mvc.service

import me.bossm0n5t3r.security.mvc.dto.UserDetail
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class UserService {
    fun getCurrentUser(): UserDetail? = SecurityContextHolder.getContext().authentication?.principal as? UserDetail
}
