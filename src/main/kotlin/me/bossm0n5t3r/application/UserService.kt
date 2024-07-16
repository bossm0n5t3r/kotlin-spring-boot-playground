package me.bossm0n5t3r.application

import me.bossm0n5t3r.application.dto.UserDto
import me.bossm0n5t3r.application.dto.UserId
import me.bossm0n5t3r.application.dto.toUserDto
import me.bossm0n5t3r.domain.User
import me.bossm0n5t3r.domain.UserEntity
import me.bossm0n5t3r.exception.NotFoundException
import me.bossm0n5t3r.presentation.dto.UserCreateRequest
import me.bossm0n5t3r.presentation.dto.UserUpdateRequest
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.springframework.stereotype.Service

@Service
class UserService(
    private val database: Database,
) {
    fun findUserById(id: UserId): UserDto =
        transaction(database) {
            addLogger(StdOutSqlLogger)
            val query =
                UserEntity
                    .selectAll()
                    .where { UserEntity.id eq id.value }

            User.wrapRows(query).toList().firstOrNull()
        }?.toUserDto() ?: throw NotFoundException("id: $id")

    fun create(request: UserCreateRequest): UserId {
        val id =
            transaction(database) {
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
    ) = transaction(database) {
        addLogger(StdOutSqlLogger)
        UserEntity.update({ UserEntity.id eq id }) {
            request.name?.let { name -> it[UserEntity.name] = name }
            request.age?.let { age -> it[UserEntity.age] = age }
        }
    }

    fun delete(id: UserId) =
        transaction(database) {
            addLogger(StdOutSqlLogger)
            UserEntity.deleteWhere { UserEntity.id eq id.value }
        }
}
