package me.bossm0n5t3r.mfatotp.totp

import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import me.bossm0n5t3r.mfatotp.support.MutableClock

@Suppress("NonAsciiCharacters")
class TotpLockManagerTest {
    private val clock = MutableClock()
    private val sut = TotpLockManager(clock)

    @Test
    fun `실패가 3회 미만이면 잠기지 않음`() {
        // given
        val username = "alice"

        // when
        repeat(2) { sut.recordFailure(username) }

        // then
        assertFalse { sut.isLocked(username) }
    }

    @Test
    fun `3회 실패하면 잠김`() {
        // given
        val username = "alice"

        // when
        repeat(3) { sut.recordFailure(username) }

        // then
        assertTrue { sut.isLocked(username) }
    }

    @Test
    fun `마지막 실패 후 1시간 직전까지는 잠김, 1시간이 지나면 해제`() {
        // given
        val username = "alice"
        repeat(3) { sut.recordFailure(username) }

        // when
        clock.advance(Duration.ofHours(1).minusNanos(1))

        // then
        assertTrue { sut.isLocked(username) }

        // when
        clock.advance(Duration.ofNanos(1))

        // then
        assertFalse { sut.isLocked(username) }
    }

    @Test
    fun `잠금이 만료된 뒤 다시 실패하면 횟수가 1부터 시작`() {
        // given
        val username = "alice"
        repeat(3) { sut.recordFailure(username) }
        clock.advance(Duration.ofHours(1))

        // when
        sut.recordFailure(username)

        // then
        assertEquals(1, sut.failureCount(username))
    }

    @Test
    fun `resetFailures 후 잠금 해제`() {
        // given
        val username = "alice"
        repeat(3) { sut.recordFailure(username) }

        // when
        sut.resetFailures(username)

        // then
        assertFalse { sut.isLocked(username) }
    }

    @Test
    fun `사용자별로 횟수를 따로 관리 (다른 사용자에게 영향 없음)`() {
        // when
        repeat(3) { sut.recordFailure("alice") }
        repeat(2) { sut.recordFailure("bob") }

        // then
        assertEquals(3, sut.failureCount("alice"))
        assertEquals(2, sut.failureCount("bob"))

        // when
        sut.resetFailures("alice")

        // then
        assertEquals(0, sut.failureCount("alice"))
        assertEquals(2, sut.failureCount("bob"))
    }

    @Test
    fun `동시에 recordFailure 를 호출해도 횟수가 정확함`() {
        // given
        val username = "alice"
        val threadCount = 8
        val failuresPerThread = 10_000
        val start = CountDownLatch(1)

        // when
        Executors.newFixedThreadPool(threadCount).use { executor ->
            val futures =
                List(threadCount) {
                    executor.submit {
                        start.await()
                        repeat(failuresPerThread) { sut.recordFailure(username) }
                    }
                }
            start.countDown()
            futures.forEach { it.get() }
        }

        // then
        assertEquals(threadCount * failuresPerThread, sut.failureCount(username))
    }
}
