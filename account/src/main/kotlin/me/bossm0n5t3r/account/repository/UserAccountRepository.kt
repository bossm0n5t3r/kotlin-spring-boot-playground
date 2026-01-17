package me.bossm0n5t3r.account.repository

import me.bossm0n5t3r.account.domain.UserAccount
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserAccountRepository : CoroutineCrudRepository<UserAccount, Long> {
    suspend fun findByUsername(username: String): UserAccount
}
