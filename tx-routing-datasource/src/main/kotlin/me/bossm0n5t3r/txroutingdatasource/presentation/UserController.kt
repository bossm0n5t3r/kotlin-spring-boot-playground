package me.bossm0n5t3r.txroutingdatasource.presentation

import me.bossm0n5t3r.txroutingdatasource.exception.NotFoundException
import me.bossm0n5t3r.txroutingdatasource.presentation.dto.UserCreateRequest
import me.bossm0n5t3r.txroutingdatasource.presentation.dto.UserCreateResponse
import me.bossm0n5t3r.txroutingdatasource.presentation.dto.UserResponse
import me.bossm0n5t3r.txroutingdatasource.presentation.dto.UserUpdateRequest
import me.bossm0n5t3r.txroutingdatasource.presentation.dto.toUserResponse
import me.bossm0n5t3r.txroutingdatasource.service.UserService
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
    fun findUserById(
        @PathVariable id: Long,
    ): UserResponse =
        userService.getUser(id)?.toUserResponse()
            ?: throw NotFoundException("id: $id")

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    fun create(
        @RequestBody request: UserCreateRequest,
    ): UserCreateResponse {
        val saved = userService.createUser(request.name, request.email)
        return UserCreateResponse(id = requireNotNull(saved.id))
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun update(
        @PathVariable id: Long,
        @RequestBody request: UserUpdateRequest,
    ) {
        userService.updateUser(
            id = id,
            name = request.name,
            email = request.email,
        )
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable id: Long,
    ) {
        userService.deleteUser(id)
    }
}
