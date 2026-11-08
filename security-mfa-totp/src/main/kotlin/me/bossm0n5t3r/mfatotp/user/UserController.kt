package me.bossm0n5t3r.mfatotp.user

import me.bossm0n5t3r.mfatotp.user.dto.CreateUserDto
import me.bossm0n5t3r.mfatotp.user.dto.UserDto
import me.bossm0n5t3r.mfatotp.user.dto.toDto
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createUser(@RequestBody createUserDto: CreateUserDto): UserDto {
        return userService.createUser(createUserDto).toDto()
    }
}
