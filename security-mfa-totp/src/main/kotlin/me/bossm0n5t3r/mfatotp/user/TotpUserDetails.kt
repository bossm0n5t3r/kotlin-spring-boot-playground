package me.bossm0n5t3r.mfatotp.user

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class TotpUserDetails(
    val user: User,
) : UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority("ROLE_USER"))
    }

    override fun getPassword(): String {
        return user.password
    }

    override fun getUsername(): String {
        return user.username
    }

    fun isMfaEnabled() = user.mfaEnabled

    fun getTotpSecretOrNull() = user.totpSecret
}
