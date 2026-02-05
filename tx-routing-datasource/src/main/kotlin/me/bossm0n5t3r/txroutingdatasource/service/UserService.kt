package me.bossm0n5t3r.txroutingdatasource.service

import me.bossm0n5t3r.txroutingdatasource.domain.entity.User
import me.bossm0n5t3r.txroutingdatasource.domain.repository.UserRepository
import me.bossm0n5t3r.txroutingdatasource.exception.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
) {
    @Transactional(readOnly = true)
    fun getUser(id: Long): User? {
        return userRepository.findById(id).orElse(null)
    }

    @Transactional
    fun createUser(name: String, email: String): User {
        return userRepository.save(User(name = name, email = email))
    }

    @Transactional
    fun updateUser(
        id: Long,
        name: String?,
        email: String?,
    ): User {
        val user =
            userRepository.findById(id).orElseThrow {
                NotFoundException("id: $id")
            }
        if (name != null) {
            user.name = name
        }
        if (email != null) {
            user.email = email
        }
        return userRepository.save(user)
    }

    @Transactional
    fun deleteUser(id: Long) {
        val user =
            userRepository.findById(id).orElseThrow {
                NotFoundException("id: $id")
            }
        userRepository.delete(user)
    }
}
