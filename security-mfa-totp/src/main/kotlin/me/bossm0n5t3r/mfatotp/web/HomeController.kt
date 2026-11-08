package me.bossm0n5t3r.mfatotp.web

import me.bossm0n5t3r.mfatotp.user.TotpUserDetails
import me.bossm0n5t3r.mfatotp.view.dashboardPage
import me.bossm0n5t3r.mfatotp.view.homePage
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.security.web.csrf.CsrfToken
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody

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
        val userDetails = authentication.principal as? TotpUserDetails
        val username = userDetails?.user?.username ?: error("Not found username")
        return dashboardPage(username = username, csrf = csrfToken)
    }
}
