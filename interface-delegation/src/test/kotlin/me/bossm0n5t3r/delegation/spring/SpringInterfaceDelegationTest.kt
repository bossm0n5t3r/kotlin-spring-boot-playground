package me.bossm0n5t3r.delegation.spring

import me.bossm0n5t3r.delegation.spring.annotation.CustomService
import me.bossm0n5t3r.delegation.spring.annotation.ServiceType
import me.bossm0n5t3r.delegation.spring.service.ServiceA
import me.bossm0n5t3r.delegation.spring.service.ServiceB
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.springframework.beans.factory.getBean
import org.springframework.beans.factory.getBeansWithAnnotation

@SpringBootTest
class SpringInterfaceDelegationTest {

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Test
    fun `Spring components should use interface delegation for common logic`() {
        // Find components by annotation
        val services = applicationContext.getBeansWithAnnotation<CustomService>()
        
        val serviceA = services.values.filterIsInstance<ServiceA>().firstOrNull()
        val serviceB = services.values.filterIsInstance<ServiceB>().firstOrNull()

        assertNotNull(serviceA)
        assertNotNull(serviceB)

        // Verify common logic is delegated and working
        assertEquals("Common Process Done", serviceA.commonProcess())
        assertEquals("Common Process Done", serviceB.commonProcess())

        // Verify specific logic
        assertEquals("Service A executing with Common Process Done", serviceA.execute())
        assertEquals("Service B executing with Common Process Done", serviceB.execute())
    }
    
    @Test
    fun `ServiceA and ServiceB should have correct CustomService annotation`() {
        val serviceA = applicationContext.getBean<ServiceA>()
        val serviceB = applicationContext.getBean<ServiceB>()
        
        val annotationA = serviceA::class.java.getAnnotation(CustomService::class.java)
        val annotationB = serviceB::class.java.getAnnotation(CustomService::class.java)
        
        assertNotNull(annotationA)
        assertEquals(ServiceType.TYPE_A, annotationA.type)
        
        assertNotNull(annotationB)
        assertEquals(ServiceType.TYPE_B, annotationB.type)
    }
}
