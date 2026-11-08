package me.bossm0n5t3r.mfatotp.view

import kotlinx.html.ButtonType
import kotlinx.html.FormMethod
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.h1
import kotlinx.html.img
import kotlinx.html.p
import org.springframework.security.web.csrf.CsrfToken

fun totpSetupPage(
    qrCode: String,
    secret: String,
    csrf: CsrfToken,
    error: String? = null,
): String {
    return layout(title = "Set Up Authenticator") {
        h1 { +"Configure TOTP" }
        subtitle("// Step 1: Link authenticator app")

        alert("error", error)

        p(classes = "info-text") {
            +"Scan the QR code below with your authenticator application to register this account."
        }

        div(classes = "qr-container") {
            img(src = qrCode, alt = "QR Code") {
                width = "180"
                height = "180"
            }
        }

        mutedText("Manual entry key:")
        div(classes = "secret-key") { +secret }

        form(action = "/totp/setup/verify", method = FormMethod.post) {
            csrfInput(csrf)
            codeInput(labelText = "Verification Code")
            button(type = ButtonType.submit) { +"Verify & Activate" }
        }

        logoutButton(csrf, text = "Cancel")
    }
}
