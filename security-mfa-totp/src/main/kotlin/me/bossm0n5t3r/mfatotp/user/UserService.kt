package me.bossm0n5t3r.mfatotp.user

import me.bossm0n5t3r.mfatotp.user.dto.CreateUserDto
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class UserService(
    private val passwordEncoder: PasswordEncoder,
    private val userStore: UserStore,
) {
    fun createUser(createUserDto: CreateUserDto): User {
        val hashedPassword = passwordEncoder.encode(createUserDto.password)
        checkNotNull(hashedPassword) { "hashedPassword should not be null" }
        return userStore.createUserOrNull(createUserDto.username, hashedPassword)
            ?: throw ResponseStatusException(HttpStatus.CONFLICT, "User already exists")
    }
}
