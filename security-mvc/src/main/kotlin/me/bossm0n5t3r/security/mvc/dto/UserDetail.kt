package me.bossm0n5t3r.security.mvc.dto

import me.bossm0n5t3r.security.mvc.enumeration.UserRole
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

data class UserDetail(
    val userId: String,
    private val username: String,
    val nickname: String,
    val email: String,
    val roles: List<UserRole>,
) : UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority> = roles.map { SimpleGrantedAuthority("ROLE_${it.name}") }

    override fun getPassword(): String = ""

    override fun getUsername(): String = username

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true
}
