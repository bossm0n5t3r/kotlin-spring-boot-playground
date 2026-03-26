plugins {
    alias(libs.plugins.kotlin.jvm)
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
