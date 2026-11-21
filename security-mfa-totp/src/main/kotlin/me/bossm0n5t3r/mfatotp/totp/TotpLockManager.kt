package me.bossm0n5t3r.mfatotp.totp

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import org.jetbrains.annotations.VisibleForTesting
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

    /**
     * Counts an attempt before the code is verified, and returns `false` without counting it if the
     * user is locked. Counting first keeps concurrent requests from exceeding the limit.
     */
    fun tryRecordAttempt(username: String): Boolean {
        val now = clock.instant()
        var recorded = false
        failures.compute(username) { _, state ->
            when {
                state == null || state.isExpired(now) ->
                    FailureState(1, now).also { recorded = true }
                state.count >= MAX_VERIFY_FAILURE_COUNT -> state
                else -> FailureState(state.count + 1, now).also { recorded = true }
            }
        }
        return recorded
    }

    fun resetFailures(username: String) {
        failures.remove(username)
    }

    @VisibleForTesting
    internal fun failureCount(username: String): Int = failures[username]?.count ?: 0

    private fun FailureState.isExpired(now: Instant = clock.instant()) =
        Duration.between(lastFailedAt, now) >= LOCK_DURATION

    companion object {
        private const val MAX_VERIFY_FAILURE_COUNT = 3
        private val LOCK_DURATION = Duration.ofHours(1)
    }
}
