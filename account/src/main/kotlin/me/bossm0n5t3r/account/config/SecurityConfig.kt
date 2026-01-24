package me.bossm0n5t3r.account.config

import me.bossm0n5t3r.account.security.BearerTokenAccessDeniedHandler
import me.bossm0n5t3r.account.security.BearerTokenAuthenticationConverter
import me.bossm0n5t3r.account.security.BearerTokenAuthenticationEntryPoint
import me.bossm0n5t3r.account.security.BearerTokenAuthenticationManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter

@Configuration
@EnableWebFluxSecurity
class SecurityConfig(
    private val authenticationManager: BearerTokenAuthenticationManager,
    private val authenticationConverter: BearerTokenAuthenticationConverter,
    private val entryPoint: BearerTokenAuthenticationEntryPoint,
    private val accessDeniedHandler: BearerTokenAccessDeniedHandler,
) {
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        val authenticationWebFilter =
            AuthenticationWebFilter(authenticationManager).apply {
                setServerAuthenticationConverter(authenticationConverter)
                setAuthenticationFailureHandler { webFilterExchange, exception ->
                    entryPoint.commence(webFilterExchange.exchange, exception)
                }
            }

        return http
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .authorizeExchange {
                it.pathMatchers("/api/account/register", "/api/account/login").permitAll()
                it.pathMatchers("/api/account/role").hasRole("ADMIN")
                it.anyExchange().authenticated()
            }.addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .exceptionHandling {
                it.authenticationEntryPoint(entryPoint)
                it.accessDeniedHandler(accessDeniedHandler)
            }.build()
    }
}
