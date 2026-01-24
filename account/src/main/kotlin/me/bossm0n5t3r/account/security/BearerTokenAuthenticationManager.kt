package me.bossm0n5t3r.account.security

import kotlinx.coroutines.reactor.mono
import me.bossm0n5t3r.account.repository.UserAccountRepository
import me.bossm0n5t3r.account.util.Constants.ROLE_PREFIX
import me.bossm0n5t3r.account.util.JwtProvider
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class BearerTokenAuthenticationManager(
    private val jwtProvider: JwtProvider,
    private val userAccountRepository: UserAccountRepository,
) : ReactiveAuthenticationManager {
    override fun authenticate(authentication: Authentication): Mono<Authentication> =
        mono {
            val token = authentication.credentials as String
            val username = jwtProvider.getUsernameFromToken(token)
            val userAccount = userAccountRepository.findByUsername(username)
            val authorities = listOf(SimpleGrantedAuthority("$ROLE_PREFIX${userAccount.role}"))

            UsernamePasswordAuthenticationToken(userAccount, token, authorities)
        }
}
