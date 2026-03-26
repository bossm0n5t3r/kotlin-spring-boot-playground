package me.bossm0n5t3r.delegation

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

enum class ServiceType {
    TYPE_A, TYPE_B
}

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class CustomService(val type: ServiceType)

interface CommonLogic {
    fun commonProcess(): String
}

@Component
class CommonLogicImpl : CommonLogic {
    override fun commonProcess(): String = "Common Process Done"
}

interface BusinessService : CommonLogic {
    fun execute(): String
}

@Component
@CustomService(ServiceType.TYPE_A)
class ServiceA(
    @param:Qualifier("commonLogicImpl") private val commonLogic: CommonLogic
) : BusinessService, CommonLogic by commonLogic {
    override fun execute(): String = "Service A executing with ${commonProcess()}"
}

@Component
@CustomService(ServiceType.TYPE_B)
class ServiceB(
    @param:Qualifier("commonLogicImpl") private val commonLogic: CommonLogic
) : BusinessService, CommonLogic by commonLogic {
    override fun execute(): String = "Service B executing with ${commonProcess()}"
}
