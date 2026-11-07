package me.bossm0n5t3r.mfatotp.user

import com.atlassian.onetime.model.TOTPSecret
import com.atlassian.onetime.service.RandomSecretProvider
import kotlin.uuid.Uuid

object UserFactory {
    fun createUser(
        hashedPassword: String,
        isMfaEnabled: Boolean = false,
    ): User {
        return when {
            !isMfaEnabled -> User(Uuid.random().toHexDashString(), hashedPassword)
            else -> {
                val secret: TOTPSecret = RandomSecretProvider.generateSecret()
                User("user", hashedPassword).apply {
                    totpSecret = secret.base32Encoded
                    mfaEnabled = true
                }
            }
        }
    }
}
