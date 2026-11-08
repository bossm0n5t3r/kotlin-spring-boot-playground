package me.bossm0n5t3r.mfatotp.view

import kotlinx.html.a
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.p
import kotlinx.html.style

fun homePage(): String {
    return layout(title = "MFA Demo - Home") {
        h1 { +"Multi-Factor Auth" }
        subtitle("// TOTP authentication demo")

        p(classes = "info-text") {
            +"Spring Security MFA implementation with Time-based One-Time Password support. "
            +"Compatible with standard authenticator applications."
        }

        a(href = "/login", classes = "btn") { +"Initialize Session" }

        div(classes = "link") { a(href = "/signup") { +"No account? Create one" } }

        p(classes = "info-text") {
            style = "margin-top: 28px; font-size: 12px; color: var(--text-muted);"
            +"Supported: Google Authenticator, Authy, Microsoft Authenticator, 1Password"
        }
    }
}
