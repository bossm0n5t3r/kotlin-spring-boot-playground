plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.ktlint)
}

group = "me.bossm0n5t3r"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion =
            JavaLanguageVersion.of(
                libs.versions.jdk
                    .get()
                    .toInt(),
            )
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.coroutines.reactor)

    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.jackson.module.kotlin)

    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.r2dbc)
    implementation(libs.exposed.json)
    implementation(libs.exposed.kotlin.datetime)
    runtimeOnly(libs.h2)
    runtimeOnly(libs.r2dbc.h2)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.kotlin.test.junit5)
    testImplementation(libs.okhttp3.mockwebserver)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.kotlinx.coroutines.test)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

ktlint {
    version.set(
        libs.versions.pinterest.ktlint
            .get(),
    )
}
