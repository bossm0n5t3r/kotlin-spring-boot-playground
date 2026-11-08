package me.bossm0n5t3r.mfatotp.view

import kotlinx.html.ButtonType
import kotlinx.html.FormMethod
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.h1
import kotlinx.html.p
import kotlinx.html.strong
import org.springframework.security.web.csrf.CsrfToken

fun totpVerifyPage(
    username: String,
    csrf: CsrfToken,
    error: String? = null,
): String {
    return layout(title = "Verify Identity") {
        h1 { +"Second Factor" }
        subtitle("// TOTP verification required")

        alert("error", error)

        div(classes = "info") {
            +"Session: "
            strong { +username }
        }

        p(classes = "info-text") {
            +"Enter the current 6-digit code from your authenticator application."
        }

        form(action = "/totp/verify", method = FormMethod.post) {
            csrfInput(csrf)
            codeInput(labelText = "TOTP Code", autoFocus = true)
            button(type = ButtonType.submit) { +"Verify" }
        }

        logoutButton(csrf, text = "Switch Account")
    }
}
