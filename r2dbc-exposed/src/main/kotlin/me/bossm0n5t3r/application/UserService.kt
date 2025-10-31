package me.bossm0n5t3r.application

import kotlinx.coroutines.flow.toList
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
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insertAndGetId
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.r2dbc.update
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class UserService(
    @Qualifier("masterDatabase") private val masterDatabase: R2dbcDatabase,
    @Qualifier("slaveDatabase") private val slaveDatabase: R2dbcDatabase,
) {
    suspend fun findUserById(id: UserId): UserDto =
        suspendTransaction(slaveDatabase) {
            addLogger(StdOutSqlLogger)
            UserEntity
                .selectAll()
                .where { UserEntity.id eq id.value }
                .toList()
                .firstOrNull()
                ?.let { User.wrapRow(it) }
        }?.toUserDto() ?: throw NotFoundException("id: $id")

    suspend fun create(request: UserCreateRequest): UserId {
        val id =
            suspendTransaction(masterDatabase) {
                addLogger(StdOutSqlLogger)
                UserEntity.insertAndGetId {
                    it[name] = request.name
                    it[age] = request.age
                }
            }

        return UserId(id.value)
    }

    suspend fun update(
        id: Long,
        request: UserUpdateRequest,
    ) = suspendTransaction(masterDatabase) {
        addLogger(StdOutSqlLogger)
        UserEntity.update({ UserEntity.id eq id }) {
            request.name?.let { name -> it[UserEntity.name] = name }
            request.age?.let { age -> it[UserEntity.age] = age }
        }
    }

    suspend fun delete(id: UserId) =
        suspendTransaction(masterDatabase) {
            addLogger(StdOutSqlLogger)
            UserEntity.deleteWhere { UserEntity.id eq id.value }
        }
}
