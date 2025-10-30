package me.bossm0n5t3r.domain

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

class User(
    id: EntityID<Long>,
) : LongEntity(id) {
    companion object : LongEntityClass<User>(UserEntity)

    var name by UserEntity.name
    var age by UserEntity.age
}
