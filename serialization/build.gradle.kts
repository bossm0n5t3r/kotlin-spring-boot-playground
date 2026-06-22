plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.ktfmt)
}

group = "me.bossm0n5t3r"

version = "0.0.1-SNAPSHOT"

java { toolchain { languageVersion = JavaLanguageVersion.of(libs.versions.jdk.get().toInt()) } }

repositories { mavenCentral() }

dependencyManagement { imports { mavenBom(libs.spring.boot.dependencies.get().toString()) } }

dependencies {
    testImplementation(libs.jackson.module.kotlin)
    testImplementation(libs.jackson.datatype.jsr310)

    testImplementation(libs.tools.jackson.module.kotlin)

    testImplementation(libs.kotlinx.serialization.json)
    testImplementation(libs.kotlinx.serialization.jvm.extra)

    testImplementation(libs.gson)

    testImplementation(libs.kotlin.test.junit5)
}

kotlin { compilerOptions { freeCompilerArgs.addAll("-Xjsr305=strict") } }

tasks.withType<Test> { useJUnitPlatform() }

ktfmt { kotlinLangStyle() }
