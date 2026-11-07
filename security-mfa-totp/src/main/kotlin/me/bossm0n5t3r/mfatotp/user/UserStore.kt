package me.bossm0n5t3r.mfatotp.user

import java.util.concurrent.ConcurrentHashMap
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class UserStore(
    private val passwordEncoder: PasswordEncoder,
) : UserDetailsService {
    private val users = ConcurrentHashMap<String, User>()

    init {
        val password = "password"
        val hashedPassword = passwordEncoder.encode(password)
        checkNotNull(hashedPassword) { "hashedPassword should not be null" }

        val mfaConfiguredUser = UserFactory.createUser(hashedPassword, true)
        val notMfaConfiguredUser = UserFactory.createUser(hashedPassword)
        users[mfaConfiguredUser.username] = mfaConfiguredUser
        users[notMfaConfiguredUser.username] = notMfaConfiguredUser
    }

    override fun loadUserByUsername(username: String): UserDetails {
        return findByUsername(username)?.let { TotpUserDetails(it) }
            ?: throw UsernameNotFoundException("User not found: $username")
    }

    private fun findByUsername(username: String) = users[username]
}
