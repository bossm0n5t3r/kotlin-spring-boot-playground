package me.bossm0n5t3r.delegation.spring.service

interface BusinessService : CommonLogic {
    fun execute(): String
}
