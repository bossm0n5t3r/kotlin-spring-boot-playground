package me.bossm0n5t3r.delegation.spring.service

import me.bossm0n5t3r.delegation.spring.annotation.CustomService
import me.bossm0n5t3r.delegation.spring.annotation.ServiceType
import org.springframework.stereotype.Component

@Component
@CustomService(ServiceType.TYPE_ACTUALLY_A)
class ServiceActuallyA(
    private val service: ServiceA
) : BusinessService, CommonLogic by service {
    override fun execute(): String = service.execute()
}
