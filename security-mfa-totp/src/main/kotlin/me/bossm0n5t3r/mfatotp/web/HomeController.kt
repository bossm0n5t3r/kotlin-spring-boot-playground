package me.bossm0n5t3r.mfatotp.web

import me.bossm0n5t3r.mfatotp.user.TotpUserDetails
import me.bossm0n5t3r.mfatotp.view.dashboardPage
import me.bossm0n5t3r.mfatotp.view.homePage
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.security.web.csrf.CsrfToken
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.server.ResponseStatusException

@Controller
class HomeController {
    @GetMapping("/", produces = [MediaType.TEXT_HTML_VALUE])
    @ResponseBody
    fun home(): String = homePage()

    @GetMapping("/dashboard", produces = [MediaType.TEXT_HTML_VALUE])
    @ResponseBody
    fun dashboard(
        authentication: Authentication,
        csrfToken: CsrfToken,
    ): String {
        val userDetails =
            authentication.principal as? TotpUserDetails
                ?: throw ResponseStatusException(HttpStatus.FORBIDDEN)
        val username = userDetails.user.username
        return dashboardPage(username = username, csrf = csrfToken)
    }
}
