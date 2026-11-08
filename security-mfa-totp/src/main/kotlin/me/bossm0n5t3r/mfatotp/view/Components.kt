package me.bossm0n5t3r.mfatotp.view

import kotlinx.html.ButtonType
import kotlinx.html.FORM
import kotlinx.html.FlowContent
import kotlinx.html.FormMethod
import kotlinx.html.InputType
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.hiddenInput
import kotlinx.html.id
import kotlinx.html.input
import kotlinx.html.label
import kotlinx.html.p
import kotlinx.html.style
import org.springframework.security.web.csrf.CsrfToken

fun FORM.csrfInput(csrf: CsrfToken) {
    hiddenInput(name = csrf.parameterName) { value = csrf.token }
}

fun FlowContent.alert(
    kind: String,
    message: String?,
) {
    message?.let { div(classes = kind) { +it } }
}

fun FlowContent.subtitle(text: String) {
    p(classes = "page-subtitle") { +text }
}

fun FlowContent.mutedText(text: String) {
    p(classes = "info-text") {
        style = "font-size: 12px; color: var(--text-muted);"
        +text
    }
}

fun FORM.codeInput(
    labelText: String,
    autoFocus: Boolean = false,
) {
    div(classes = "form-group") {
        label {
            htmlFor = "code"
            +labelText
        }
        input(type = InputType.text, name = "code", classes = "code-input") {
            id = "code"
            maxLength = "6"
            pattern = "[0-9]{6}"
            placeholder = "000000"
            required = true
            this.autoFocus = autoFocus
            attributes["inputmode"] = "numeric"
            attributes["autocomplete"] = "one-time-code"
        }
    }
}

fun FlowContent.logoutButton(
    csrf: CsrfToken,
    text: String,
) {
    div(classes = "link") {
        style = "margin-top: 28px;"
        form(action = "/logout", method = FormMethod.post) {
            style = "display: inline;"
            csrfInput(csrf)
            button(type = ButtonType.submit, classes = "btn btn-secondary") {
                style = "width: auto; padding: 12px 20px;"
                +text
            }
        }
    }
}
