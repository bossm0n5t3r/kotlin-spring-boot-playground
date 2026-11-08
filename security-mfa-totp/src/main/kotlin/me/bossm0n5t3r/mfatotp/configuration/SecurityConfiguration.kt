package me.bossm0n5t3r.mfatotp.configuration

import me.bossm0n5t3r.mfatotp.totp.TotpAuthenticationHandler
import me.bossm0n5t3r.mfatotp.user.UserStore
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authorization.EnableMultiFactorAuthentication
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.authority.FactorGrantedAuthority
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
@EnableMultiFactorAuthentication(authorities = [])
class SecurityConfiguration(
    private val userStore: UserStore,
    private val totpAuthenticationHandler: TotpAuthenticationHandler,
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .authorizeHttpRequests { auth ->
                auth
                    // Public pages
                    .requestMatchers("/", "/error", "/css/**", "/js/**")
                    .permitAll()
                    // TOTP pages - only need password factor
                    .requestMatchers("/totp/**")
                    .hasAuthority(FactorGrantedAuthority.PASSWORD_AUTHORITY)
                    // Protected pages - need TOTP factor (which implies password was done first)
                    .requestMatchers("/dashboard/**")
                    .hasAuthority(TotpAuthenticationHandler.TOTP_AUTHORITY)
                    .anyRequest()
                    .authenticated()
            }
            .formLogin { form -> form.successHandler(totpAuthenticationHandler).permitAll() }
            .logout { logout -> logout.logoutSuccessUrl("/?logout").permitAll() }
            .userDetailsService(userStore)
            .build()
    }
}
