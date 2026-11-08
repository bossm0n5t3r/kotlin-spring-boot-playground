package me.bossm0n5t3r.mfatotp.user

import java.util.concurrent.ConcurrentHashMap
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component

@Component
class UserStore : UserDetailsService {
    private val users = ConcurrentHashMap<String, User>()

    override fun loadUserByUsername(username: String): UserDetails {
        return findByUsernameOrNull(username)?.let { TotpUserDetails(it) }
            ?: throw UsernameNotFoundException("User not found: $username")
    }

    fun createUserOrNull(username: String, hashedPassword: String): User? {
        val user = User(username, hashedPassword)
        return if (users.putIfAbsent(username, user) == null) user else null
    }

    fun findByUsernameOrNull(username: String) = users[username]
}
