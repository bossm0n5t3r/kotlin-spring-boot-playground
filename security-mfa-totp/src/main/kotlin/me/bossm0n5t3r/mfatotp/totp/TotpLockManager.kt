package me.bossm0n5t3r.mfatotp.totp

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import org.springframework.stereotype.Component

@Component
class TotpLockManager(
    private val clock: Clock,
) {
    private data class FailureState(val count: Int, val lastFailedAt: Instant)

    private val failures = ConcurrentHashMap<String, FailureState>()

    fun isLocked(username: String): Boolean {
        val state = failures[username] ?: return false
        return state.count >= MAX_VERIFY_FAILURE_COUNT && !state.isExpired()
    }

    fun recordFailure(username: String) {
        val now = clock.instant()
        failures.compute(username) { _, state ->
            if (state == null || state.isExpired(now)) FailureState(1, now)
            else FailureState(state.count + 1, now)
        }
    }

    fun resetFailures(username: String) {
        failures.remove(username)
    }

    private fun FailureState.isExpired(now: Instant = clock.instant()) =
        Duration.between(lastFailedAt, now) >= LOCK_DURATION

    companion object {
        private const val MAX_VERIFY_FAILURE_COUNT = 3
        private val LOCK_DURATION = Duration.ofHours(1)
    }
}
