package me.bossm0n5t3r.mfatotp.view

import kotlinx.html.DIV
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.head
import kotlinx.html.header
import kotlinx.html.html
import kotlinx.html.lang
import kotlinx.html.link
import kotlinx.html.main
import kotlinx.html.meta
import kotlinx.html.stream.createHTML
import kotlinx.html.title

private const val FONTS_URL =
    "https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@400;500;600;700&family=Source+Sans+3:wght@400;500;600&display=swap"

fun layout(
    title: String = "MFA Demo",
    content: DIV.() -> Unit,
): String {
    return "<!DOCTYPE html>" +
        createHTML().html {
            lang = "en"
            head {
                meta(charset = "UTF-8")
                meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
                title(title)
                link(rel = "preconnect", href = "https://fonts.googleapis.com")
                link(rel = "preconnect", href = "https://fonts.gstatic.com") {
                    attributes["crossorigin"] = ""
                }
                link(rel = "stylesheet", href = FONTS_URL)
                link(rel = "stylesheet", href = "/css/main.css")
            }
            body {
                header(classes = "site-header") { div(classes = "brand") { +"Secure Auth" } }
                main(classes = "main-wrapper") { div(classes = "container") { content() } }
            }
        }
}
