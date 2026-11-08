package me.bossm0n5t3r.mfatotp.web

import me.bossm0n5t3r.mfatotp.view.loginPage
import org.springframework.http.MediaType
import org.springframework.security.web.csrf.CsrfToken
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class LoginController {
    @GetMapping("/login", produces = [MediaType.TEXT_HTML_VALUE])
    @ResponseBody
    fun login(
        @RequestParam error: String?,
        @RequestParam logout: String?,
        @RequestParam locked: String?,
        csrfToken: CsrfToken,
    ): String =
        loginPage(
            csrfToken,
            locked?.let { "Too many failed attempts. Please try again later." }
                ?: error?.let { "Invalid username or password" },
            logout?.let { "You have been logged out" },
        )
}
