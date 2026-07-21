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

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation(libs.kotlin.reflect)

    implementation(libs.spring.boot.starter.web)
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation(libs.jackson.module.kotlin)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.kotlin.test.junit5)
    testRuntimeOnly(libs.junit.platform.launcher)

    implementation(libs.readability4k.bossm0n5t3r)
    implementation(libs.readability4k.sermilion)
}

kotlin { compilerOptions { freeCompilerArgs.addAll("-Xjsr305=strict") } }

tasks.withType<Test> { useJUnitPlatform() }

ktfmt { kotlinLangStyle() }
