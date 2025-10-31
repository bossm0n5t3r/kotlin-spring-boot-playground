package me.bossm0n5t3r.application

import me.bossm0n5t3r.application.dto.UserDto
import me.bossm0n5t3r.application.dto.UserId
import me.bossm0n5t3r.application.dto.toUserDto
import me.bossm0n5t3r.domain.User
import me.bossm0n5t3r.domain.UserEntity
import me.bossm0n5t3r.exception.NotFoundException
import me.bossm0n5t3r.presentation.dto.UserCreateRequest
import me.bossm0n5t3r.presentation.dto.UserUpdateRequest
import org.jetbrains.exposed.v1.core.StdOutSqlLogger
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class UserService(
    @Qualifier("masterDatabase") private val masterDatabase: Database,
    @Qualifier("slaveDatabase") private val slaveDatabase: Database,
) {
    fun findUserById(id: UserId): UserDto =
        transaction(slaveDatabase) {
            addLogger(StdOutSqlLogger)
            val query =
                UserEntity
                    .selectAll()
                    .where { UserEntity.id eq id.value }

            User.wrapRows(query).toList().firstOrNull()
        }?.toUserDto() ?: throw NotFoundException("id: $id")

    fun create(request: UserCreateRequest): UserId {
        val id =
            transaction(masterDatabase) {
                addLogger(StdOutSqlLogger)
                UserEntity.insertAndGetId {
                    it[name] = request.name
                    it[age] = request.age
                }
            }

        return UserId(id.value)
    }

    fun update(
        id: Long,
        request: UserUpdateRequest,
    ) = transaction(masterDatabase) {
        addLogger(StdOutSqlLogger)
        UserEntity.update({ UserEntity.id eq id }) {
            request.name?.let { name -> it[UserEntity.name] = name }
            request.age?.let { age -> it[UserEntity.age] = age }
        }
    }

    fun delete(id: UserId) =
        transaction(masterDatabase) {
            addLogger(StdOutSqlLogger)
            UserEntity.deleteWhere { UserEntity.id eq id.value }
        }
}
