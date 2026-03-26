# Kotlin Interface Delegation

이 모듈은 Kotlin의 **Interface Delegation (인터페이스 위임)** 기능을 설명하고 예제 코드를 제공합니다.

## Interface Delegation이란?

인터페이스 위임은 객체 지향 설계 패턴 중 하나인 **데코레이터 패턴(Decorator Pattern)**을 언어 차원에서 직접 지원하는 기능입니다.

상속(Inheritance) 대신 컴포지션(Composition) 을 사용하여 코드의 재사용성을 높이고 결합도를 낮출 수 있도록 돕습니다.

Kotlin에서는 `by` 키워드를 사용하여 인터페이스의 구현을 다른 객체에 위임할 수 있습니다.

### 특징

- **보일러플레이트 코드 감소**: 인터페이스의 모든 메서드를 직접 오버라이드하여 호출을 전달하는 코드를 작성할 필요가 없습니다.
- **컴포지션 활용**: 상속을 통하지 않고도 기능을 확장하거나 재사용할 수 있습니다.
- **선택적 오버라이드**: 위임된 인터페이스의 메서드 중 일부만 직접 구현하고 나머지는 위임 대상 객체(delegate)가 처리하도록 둘 수 있습니다.

## Spring Boot와 함께 사용하기

인터페이스 위임은 Spring Boot의 `@Component`와 함께 사용하여 공통 로직을 깔끔하게 분리할 수 있습니다.

### Custom Annotation 및 Enum

```kotlin
enum class ServiceType { TYPE_A, TYPE_B }

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class CustomService(val type: ServiceType)
```

### 인터페이스 위임이 적용된 Component

```kotlin
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
    @Qualifier("commonLogicImpl") private val commonLogic: CommonLogic
) : BusinessService, CommonLogic by commonLogic {
    override fun execute(): String = "Service A executing with ${commonProcess()}"
}
```

## 테스트 코드

다음 경로에서 인터페이스 위임 기능이 정상적으로 동작하는지 확인하는 테스트 코드를 찾아볼 수 있습니다.

- **기본 예제 테스트**: `src/test/kotlin/me/bossm0n5t3r/delegation/InterfaceDelegationTest.kt`
- **Spring Boot 예제 테스트**: `src/test/kotlin/me/bossm0n5t3r/delegation/SpringInterfaceDelegationTest.kt`

위임 기능이 정상적으로 동작하는지 다음과 같이 검증합니다:
- 위임된 메서드가 정상적으로 호출되는지 확인
- 일부 메서드를 명시적으로 오버라이드했을 때의 동작 확인
- Spring Context 내에서 어노테이션 기반 빈 조회 및 동작 확인
