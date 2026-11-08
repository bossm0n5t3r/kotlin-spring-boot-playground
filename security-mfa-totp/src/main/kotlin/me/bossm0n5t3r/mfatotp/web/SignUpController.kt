package me.bossm0n5t3r.mfatotp.web

import me.bossm0n5t3r.mfatotp.user.UserService
import me.bossm0n5t3r.mfatotp.user.dto.CreateUserDto
import me.bossm0n5t3r.mfatotp.view.signupPage
import org.springframework.http.MediaType
import org.springframework.security.web.csrf.CsrfToken
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.server.ResponseStatusException

@Controller
class SignUpController(
    private val userService: UserService,
) {
    @GetMapping("/signup", produces = [MediaType.TEXT_HTML_VALUE])
    @ResponseBody
    fun signUp(
        @RequestParam error: String?,
        csrfToken: CsrfToken,
    ): String =
        signupPage(
            csrf = csrfToken,
            error = error?.let { "Username already exists" },
        )

    @PostMapping("/signup", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun signUp(@ModelAttribute createUserDto: CreateUserDto): String =
        try {
            userService.createUser(createUserDto)
            "redirect:/login"
        } catch (_: ResponseStatusException) {
            "redirect:/signup?error"
        }
}
