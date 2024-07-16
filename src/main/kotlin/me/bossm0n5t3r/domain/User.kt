package me.bossm0n5t3r.domain

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class User(
    id: EntityID<Long>,
) : LongEntity(id) {
    companion object : LongEntityClass<User>(UserEntity)

    var name by UserEntity.name
    var age by UserEntity.age
}
