package me.bossm0n5t3r.delegation.spring.service

import me.bossm0n5t3r.delegation.spring.annotation.CustomService
import me.bossm0n5t3r.delegation.spring.annotation.ServiceType
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
@CustomService(ServiceType.TYPE_B)
class ServiceB(
    @param:Qualifier("commonLogicImpl") private val commonLogic: CommonLogic
) : BusinessService, CommonLogic by commonLogic {
    override fun execute(): String = "Service B executing with ${commonProcess()}"
}
