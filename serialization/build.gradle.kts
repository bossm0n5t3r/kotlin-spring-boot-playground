plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)
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
    testImplementation(libs.tools.jackson.module.kotlin)

    testImplementation(libs.kotlinx.serialization.json)
    testImplementation(libs.kotlinx.serialization.jvm.extra)

    testImplementation(libs.gson)

    testImplementation(libs.kotlin.test.junit5)
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
