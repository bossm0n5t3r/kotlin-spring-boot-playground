package me.bossm0n5t3r.txroutingdatasource.domain.repository

import me.bossm0n5t3r.txroutingdatasource.domain.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long>
