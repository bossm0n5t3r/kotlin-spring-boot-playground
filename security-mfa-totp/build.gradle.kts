plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.ktfmt)
}

group = "me.bossm0n5t3r"

version = "0.0.1-SNAPSHOT"

java { toolchain { languageVersion = JavaLanguageVersion.of(libs.versions.jdk.get().toInt()) } }

repositories { mavenCentral() }

dependencies {
    // Spring Boot Starters
    implementation(libs.spring.boot.starter.webmvc)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.kotlin.reflect)

    // kotlinx.html
    implementation(libs.kotlinx.html.jvm)

    // TOTP Library
    implementation(libs.atlassian.onetime)

    // QR Code
    implementation(libs.zxing.javase)

    // Dev Tools
    developmentOnly(libs.spring.boot.devtools)

    // Test
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.starter.webmvc.test)
    testImplementation(libs.spring.boot.starter.security.test)
    testImplementation(libs.kotlin.test.junit5)
    testRuntimeOnly(libs.junit.platform.launcher)
}

kotlin {
    compilerOptions { freeCompilerArgs.addAll("-Xjsr305=strict", "-Xemit-jvm-type-annotations") }
}

tasks.withType<Test> { useJUnitPlatform() }

ktfmt { kotlinLangStyle() }
