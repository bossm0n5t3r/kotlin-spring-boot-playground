package me.bossm0n5t3r.delegation.spring.annotation

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class CustomService(val type: ServiceType)
