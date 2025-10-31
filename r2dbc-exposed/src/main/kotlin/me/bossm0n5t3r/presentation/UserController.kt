package me.bossm0n5t3r.presentation

import me.bossm0n5t3r.application.UserService
import me.bossm0n5t3r.application.dto.UserId
import me.bossm0n5t3r.presentation.dto.UserCreateRequest
import me.bossm0n5t3r.presentation.dto.UserCreateResponse
import me.bossm0n5t3r.presentation.dto.UserResponse
import me.bossm0n5t3r.presentation.dto.UserUpdateRequest
import me.bossm0n5t3r.presentation.dto.toUserResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService,
) {
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    suspend fun findUserById(
        @PathVariable id: Long,
    ): UserResponse =
        userService
            .findUserById(UserId(id))
            .toUserResponse()

    // Create User
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    suspend fun create(
        @RequestBody form: UserCreateRequest,
    ): UserCreateResponse {
        val userId = userService.create(form)
        return UserCreateResponse(id = userId.value)
    }

    // Update User
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    suspend fun update(
        @PathVariable id: Long,
        @RequestBody form: UserUpdateRequest,
    ) {
        userService.update(
            id = id,
            request = form,
        )
    }

    // Delete User
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun delete(
        @PathVariable id: Long,
    ) {
        userService.delete(UserId(id))
    }
}
