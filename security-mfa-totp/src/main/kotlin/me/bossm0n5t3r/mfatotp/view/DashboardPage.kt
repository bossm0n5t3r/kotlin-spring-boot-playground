package me.bossm0n5t3r.mfatotp.view

import kotlinx.html.ButtonType
import kotlinx.html.FormMethod
import kotlinx.html.button
import kotlinx.html.code
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.h1
import kotlinx.html.h3
import kotlinx.html.p
import kotlinx.html.span
import kotlinx.html.strong
import kotlinx.html.style
import org.springframework.security.web.csrf.CsrfToken

fun dashboardPage(
    username: String,
    csrf: CsrfToken,
): String {
    return layout(title = "Dashboard") {
        h1 { +"Secure Area" }
        subtitle("// Access granted")

        alert("success", "MFA verification complete")

        p(classes = "info-text") {
            +"Authenticated as "
            strong { +username }
            +". Full access to protected resources enabled."
        }

        div(classes = "status-box") {
            h3 { +"Auth Status" }
            listOf("Factor 1: Credentials verified", "Factor 2: TOTP confirmed").forEach { status ->
                div(classes = "status-item") {
                    span(classes = "check") { +"✔" }
                    span { +status }
                }
            }
        }

        p(classes = "info-text") {
            style = "font-size: 12px; color: var(--text-muted);"
            +"This resource requires "
            code { +"FACTOR_TOTP" }
            +" authority."
        }

        form(action = "/logout", method = FormMethod.post) {
            csrfInput(csrf)
            button(type = ButtonType.submit) { +"End Session" }
        }
    }
}
