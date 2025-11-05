package me.bossm0n5t3r.application

import me.bossm0n5t3r.application.dto.UserDto
import me.bossm0n5t3r.application.dto.UserId
import me.bossm0n5t3r.application.dto.toUserDto
import me.bossm0n5t3r.domain.entity.User
import me.bossm0n5t3r.domain.repository.read.UserReadRepository
import me.bossm0n5t3r.domain.repository.write.UserWriteRepository
import me.bossm0n5t3r.exception.NotFoundException
import me.bossm0n5t3r.presentation.dto.UserCreateRequest
import me.bossm0n5t3r.presentation.dto.UserUpdateRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userReadRepository: UserReadRepository,
    private val userWriteRepository: UserWriteRepository,
) {
    @Transactional(readOnly = true)
    fun findUserById(id: UserId): UserDto =
        userReadRepository.findByIdOrNull(id.value)?.toUserDto()
            ?: throw NotFoundException("id: $id")

    @Transactional
    fun create(request: UserCreateRequest): UserId {
        val user =
            User(
                name = request.name,
                age = request.age,
            )
        val saved = userWriteRepository.save(user)
        return UserId(requireNotNull(saved.id))
    }

    @Transactional
    fun update(
        id: Long,
        request: UserUpdateRequest,
    ) {
        val user =
            userWriteRepository.findByIdOrNull(id)
                ?: throw NotFoundException("id: $id")
        user.name = request.name ?: user.name
        user.age = request.age ?: user.age
        userWriteRepository.save(user)
    }

    @Transactional
    fun delete(id: UserId) =
        userReadRepository.findByIdOrNull(id.value)?.let {
            userWriteRepository.delete(it)
        }
}
