package me.bossm0n5t3r.mfatotp.view

import kotlinx.html.ButtonType
import kotlinx.html.FormMethod
import kotlinx.html.InputType
import kotlinx.html.a
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.h1
import kotlinx.html.id
import kotlinx.html.input
import kotlinx.html.label
import org.springframework.security.web.csrf.CsrfToken

fun loginPage(
    csrf: CsrfToken,
    error: String? = null,
    message: String? = null,
): String {
    return layout(title = "Sign In") {
        h1 { +"Authentication" }
        subtitle("// Enter credentials")

        alert("error", error)
        alert("success", message)

        form(action = "/login", method = FormMethod.post) {
            csrfInput(csrf)
            div(classes = "form-group") {
                label {
                    htmlFor = "username"
                    +"Username"
                }
                input(type = InputType.text, name = "username") {
                    id = "username"
                    placeholder = "Enter username"
                    required = true
                    autoFocus = true
                }
            }
            div(classes = "form-group") {
                label {
                    htmlFor = "password"
                    +"Password"
                }
                input(type = InputType.password, name = "password") {
                    id = "password"
                    placeholder = "Enter password"
                    required = true
                }
            }
            button(type = ButtonType.submit) { +"Authenticate" }
        }

        div(classes = "link") { a(href = "/signup") { +"No account? Create one" } }
        div(classes = "link") { a(href = "/") { +"← Back" } }
    }
}
