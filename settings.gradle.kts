plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "kotlin-spring-boot-playground"

include("jdbc-exposed")
include("jdbc-jpa")
include("r2dbc-exposed")
include("sse")
include("http-exchange")
include("serialization")
include("kotlin-null-safety")
include("content-negotiation")
include("security-webflux")
include("security-mvc")
include("account")
include("tx-routing-datasource")
include("interface-delegation")
include("readability4k")
include("security-mfa-totp")
