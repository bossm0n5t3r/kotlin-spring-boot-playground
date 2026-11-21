package me.bossm0n5t3r.mfatotp.support

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

/**
 * Replaces the system clock with a [MutableClock] so that `TOTPGenerator` and `TotpLockManager`
 * share a time the test can control.
 */
@TestConfiguration
class MutableClockConfiguration {
    @Bean @Primary fun mutableClock(): MutableClock = MutableClock()
}
