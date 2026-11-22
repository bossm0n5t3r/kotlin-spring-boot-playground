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
    implementation(libs.spring.boot.starter.webmvc)
    implementation(libs.spring.boot.starter.cache)
    implementation(libs.kotlin.reflect)
    implementation(libs.caffeine)

    // Test
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.starter.webmvc.test)
    testImplementation(libs.kotlin.test.junit5)
    testRuntimeOnly(libs.junit.platform.launcher)
}

kotlin {
    compilerOptions { freeCompilerArgs.addAll("-Xjsr305=strict", "-Xemit-jvm-type-annotations") }
}

tasks.withType<Test> { useJUnitPlatform() }

ktfmt { kotlinLangStyle() }
