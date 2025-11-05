package me.bossm0n5t3r.domain.repository.read

import me.bossm0n5t3r.domain.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserReadRepository : JpaRepository<User, Long>
