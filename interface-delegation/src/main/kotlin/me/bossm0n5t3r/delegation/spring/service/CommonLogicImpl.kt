package me.bossm0n5t3r.delegation.spring.service

import org.springframework.stereotype.Component

@Component
class CommonLogicImpl : CommonLogic {
    override fun commonProcess(): String = "Common Process Done"
}
