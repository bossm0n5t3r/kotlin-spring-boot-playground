package me.bossm0n5t3r.domain

import org.jetbrains.exposed.dao.id.LongIdTable

object UserEntity : LongIdTable() {
    val name = varchar("name", length = 50)
    val age = integer("age")
}
