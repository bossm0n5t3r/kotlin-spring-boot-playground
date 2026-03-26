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

## 예제 코드

### 인터페이스 및 구현체

```kotlin
interface SoundSource {
    fun make(): String
}

class DogSound : SoundSource {
    override fun make() = "Woof"
}

class CatSound : SoundSource {
    override fun make() = "Meow"
}
```

### 인터페이스 위임 적용

```kotlin
// Animal 클래스는 SoundSource 인터페이스를 구현하며, 
// 그 실제 구현은 생성자로 전달받은 sound 객체에 위임합니다.
class Animal(sound: SoundSource) : SoundSource by sound
```

### 사용 예시

```kotlin
val dog = Animal(DogSound())
println(dog.make()) // "Woof" (DogSound의 make 호출)

val cat = Animal(CatSound())
println(cat.make()) // "Meow" (CatSound의 make 호출)
```

## 테스트 코드

`src/test/kotlin/me/bossm0n5t3r/delegation/InterfaceDelegationTest.kt`에서 위임 기능이 정상적으로 동작하는지 확인하는 테스트 코드를 찾아볼 수 있습니다.

- 위임된 메서드가 정상적으로 호출되는지 확인
- 일부 메서드를 명시적으로 오버라이드했을 때의 동작 확인
