package me.bossm0n5t3r.account.domain

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("USER_ACCOUNT")
data class UserAccount(
    @Id
    val id: Long? = null,
    val username: String,
    val nickname: String,
    val email: String,
)
