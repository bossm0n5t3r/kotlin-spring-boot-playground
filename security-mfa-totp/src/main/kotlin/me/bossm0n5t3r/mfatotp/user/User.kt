package me.bossm0n5t3r.mfatotp.user

import java.util.concurrent.atomic.AtomicLong

class User(
    val username: String,
    val password: String,
) {
    var totpSecret: String? = null
    var mfaEnabled = false

    private val lastUsedTotpStep = AtomicLong(Long.MIN_VALUE)

    fun useTotpStep(step: Long): Boolean = lastUsedTotpStep.getAndAccumulate(step, ::maxOf) < step
}
