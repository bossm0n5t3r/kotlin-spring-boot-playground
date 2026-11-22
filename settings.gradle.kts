plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "kotlin-spring-boot-playground"

include("account")
include("content-negotiation")
include("http-exchange")
include("interface-delegation")
include("jdbc-exposed")
include("jdbc-jpa")
include("kotlin-null-safety")
include("r2dbc-exposed")
include("readability4k")
include("security-mfa-totp")
include("security-mvc")
include("security-webflux")
include("serialization")
include("sse")
include("tx-routing-datasource")
