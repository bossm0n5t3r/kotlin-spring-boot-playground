package me.bossm0n5t3r.mfatotp.totp

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.net.URI
import me.bossm0n5t3r.mfatotp.user.TotpUserDetails
import me.bossm0n5t3r.mfatotp.view.totpSetupPage
import me.bossm0n5t3r.mfatotp.view.totpVerifyPage
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.csrf.CsrfToken
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.server.ResponseStatusException

@Controller
@RequestMapping("/totp")
class TotpController(
    private val totpService: TotpService,
    private val totpLockManager: TotpLockManager,
    private val totpAuthenticationHandler: TotpAuthenticationHandler,
) {
    @GetMapping("/setup", produces = [MediaType.TEXT_HTML_VALUE])
    @ResponseBody
    fun setup(csrfToken: CsrfToken): ResponseEntity<String> {
        val user =
            getCurrentUserDetailsOrNull()?.user
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

        if (user.mfaEnabled) {
            return ResponseEntity.status(HttpStatus.SEE_OTHER).location(URI("/totp/verify")).build()
        }

        val userTotpSecret = user.totpSecret ?: totpService.generateSecret()
        if (user.totpSecret == null) user.totpSecret = userTotpSecret

        return ResponseEntity.ok(
            totpSetupPage(
                qrCode = totpService.generateQrCodeDataUri(user.username, userTotpSecret),
                secret = userTotpSecret,
                csrf = csrfToken,
            ),
        )
    }

    @PostMapping("/setup/verify", produces = [MediaType.TEXT_HTML_VALUE])
    @ResponseBody
    fun setupVerify(
        request: HttpServletRequest,
        response: HttpServletResponse,
        @RequestParam code: String? = null,
        csrfToken: CsrfToken,
    ): ResponseEntity<String> {
        val authentication =
            getAuthenticationOrNull() ?: throw ResponseStatusException(HttpStatus.FORBIDDEN)

        val user =
            authentication.getCurrentUserDetailOrNull()?.user
                ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST)

        if (user.mfaEnabled) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        }

        val totpSecret = user.totpSecret ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST)

        if (totpLockManager.isLocked(user.username)) {
            request.session.invalidate()
            return ResponseEntity.status(HttpStatus.SEE_OTHER)
                .location(URI("/login?locked"))
                .build()
        }

        if (totpService.verifyCode(totpSecret, code)) {
            totpLockManager.resetFailures(user.username)
            user.mfaEnabled = true
            totpAuthenticationHandler.grantTotpFactor(
                authentication = authentication,
                request = request,
                response = response,
            )
            return ResponseEntity.status(HttpStatus.SEE_OTHER).location(URI("/dashboard")).build()
        }

        totpLockManager.recordFailure(user.username)
        return ResponseEntity.ok(
            totpSetupPage(
                qrCode =
                    totpService.generateQrCodeDataUri(
                        username = user.username,
                        secret = totpSecret,
                    ),
                secret = totpSecret,
                csrf = csrfToken,
                error = "Invalid code. Please try again.",
            ),
        )
    }

    @GetMapping("/verify", produces = [MediaType.TEXT_HTML_VALUE])
    @ResponseBody
    fun showVerify(csrfToken: CsrfToken): String {
        val user =
            getCurrentUserDetailsOrNull()?.user
                ?: throw ResponseStatusException(HttpStatus.FORBIDDEN)
        return totpVerifyPage(
            username = user.username,
            csrf = csrfToken,
        )
    }

    @PostMapping("/verify", produces = [MediaType.TEXT_HTML_VALUE])
    @ResponseBody
    fun verify(
        request: HttpServletRequest,
        response: HttpServletResponse,
        @RequestParam code: String? = null,
        csrfToken: CsrfToken,
    ): ResponseEntity<String> {
        val authentication =
            getAuthenticationOrNull() ?: throw ResponseStatusException(HttpStatus.FORBIDDEN)

        val user =
            authentication.getCurrentUserDetailOrNull()?.user
                ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST)

        if (totpLockManager.isLocked(user.username)) {
            request.session.invalidate()
            return ResponseEntity.status(HttpStatus.SEE_OTHER)
                .location(URI("/login?locked"))
                .build()
        }

        if (totpService.verifyCode(user.totpSecret, code)) {
            totpLockManager.resetFailures(user.username)
            totpAuthenticationHandler.grantTotpFactor(
                authentication = authentication,
                request = request,
                response = response,
            )
            return ResponseEntity.status(HttpStatus.SEE_OTHER).location(URI("/dashboard")).build()
        }

        totpLockManager.recordFailure(user.username)
        return ResponseEntity.ok(
            totpVerifyPage(
                username = user.username,
                csrf = csrfToken,
                error = "Invalid code. Please try again.",
            ),
        )
    }

    private fun getAuthenticationOrNull(): Authentication? {
        return SecurityContextHolder.getContext().authentication
    }

    private fun getCurrentUserDetailsOrNull(): TotpUserDetails? {
        return getAuthenticationOrNull()?.principal as? TotpUserDetails
    }

    private fun Authentication?.getCurrentUserDetailOrNull(): TotpUserDetails? {
        return this?.principal as? TotpUserDetails
    }
}
