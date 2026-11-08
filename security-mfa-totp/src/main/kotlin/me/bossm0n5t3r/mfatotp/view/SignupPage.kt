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

fun signupPage(
    csrf: CsrfToken,
    error: String? = null,
): String {
    return layout(title = "Sign Up") {
        h1 { +"Create Account" }
        subtitle("// Register new credentials")

        alert("error", error)

        form(action = "/signup", method = FormMethod.post) {
            csrfInput(csrf)
            div(classes = "form-group") {
                label {
                    htmlFor = "username"
                    +"Username"
                }
                input(type = InputType.text, name = "username") {
                    id = "username"
                    placeholder = "Choose username"
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
                    placeholder = "Choose password"
                    required = true
                    attributes["autocomplete"] = "new-password"
                }
            }
            button(type = ButtonType.submit) { +"Register" }
        }

        div(classes = "link") { a(href = "/login") { +"Already have an account? Sign in" } }
    }
}
