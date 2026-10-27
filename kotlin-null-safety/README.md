# Kotlin Null Safety

- Spring MVC Controller의 `@RequestBody` 파라미터에 Kotlin DTO를 직접 선언하고, `nullable × @field:NotNull × @Valid`의 8개 조합을 비교합니다.
- 중첩 DTO의 cascade 검증과 Map 자체·값·값 내부의 검증 경계도 별도 API로 비교합니다.
- 기존 scalar API를 재사용하여 Jackson의 `FAIL_ON_NULL_FOR_PRIMITIVES` 옵션 true/false에 따른 primitive 기본값 변환도 비교합니다.
- String으로 받거나 수동으로 JSON을 파싱하지 않습니다.

## 실행

```bash
./gradlew :kotlin-null-safety:bootRun
./gradlew :kotlin-null-safety:test
```

- 요청 예시:
  - [단일 필드 8개 조합](../http/kotlin-null-safety.http)
  - [중첩 DTO](../http/kotlin-null-safety-nested.http)
  - [Map](../http/kotlin-null-safety-map.http)
  - [Primitive null 옵션 비교](../http/kotlin-null-safety-primitive-null.http)

## 8개 조합

- 모든 경로는 `POST /api/null-safety/ages` 아래에 있습니다.
- `plain`은 `@NotNull=X`, `not-null`은 `@field:NotNull=O`, `/valid` 접미사는 Controller 파라미터의 `@Valid=O`를 뜻합니다.
- O는 적용, X는 미적용입니다.
- 현재 프로젝트의 Spring Boot 4.1.2 / Jackson 3 + Kotlin 모듈 기본 설정 기준입니다.

| 경로                       | nullable | @NotNull | @Valid | 누락 / 명시적 null        |
|----------------------------|----------|----------|--------|---------------------------|
| `/non-null/plain`          | X        | X        | X      | 400, Jackson 거부         |
| `/non-null/plain/valid`    | X        | X        | O      | 400, Jackson 거부         |
| `/non-null/not-null`       | X        | O        | X      | 400, Jackson 거부         |
| `/non-null/not-null/valid` | X        | O        | O      | 400, Jackson 거부         |
| `/nullable/plain`          | O        | X        | X      | 200, `{"age":null}`       |
| `/nullable/plain/valid`    | O        | X        | O      | 200, `{"age":null}`       |
| `/nullable/not-null`       | O        | O        | X      | 200, `{"age":null}`       |
| `/nullable/not-null/valid` | O        | O        | O      | 400, `{"fields":["age"]}` |

- 누락 입력은 `{}`, 명시적 null 입력은 `{"age":null}`입니다.
- 명시적 `{"age":0}`과 `{"age":25}`는 8개 조합 모두 200으로 입력값을 반환합니다.
- 0은 이 예제에서 유효하며 별도의 나이 범위 제약은 없습니다.

## 해석

- non-null `Int`는 현재 Jackson 설정에서 누락/null을 역직렬화 단계에서 거부합니다.
  - `@NotNull`, `@Valid` 유무와 무관하며 Controller 메서드 실행 전에 400이 됩니다.
- `@Valid`는 제약을 실행할 뿐, nullable 타입을 필수 값으로 바꾸지 않습니다.
  - 따라서 `Int?`에 제약이 없으면 `@Valid`가 있어도 null을 허용합니다.
- `@field:NotNull`은 필드에 제약을 선언합니다.
  - 이 DTO 직접 바인딩 경로에서는 Controller 파라미터에 `@Valid`가 없으면 해당 제약이 실행되지 않습니다.
- nullable 입력의 null을 Bean Validation으로 차단하는 조합은 `@field:NotNull val age: Int?`와 `@Valid @RequestBody`를 함께 사용하는 경우입니다.
  - nullable 선언이 `@Valid` 사용의 필수 조건이라는 뜻은 아닙니다.
- 단일 필드 예제의 요청 DTO는 타입/제약에 따라 4개이며, 각각 `@Valid` 유무로 2개 API에서 사용합니다.
  - 응답 DTO는 같은 형태로 비교하기 위해 `age: Int?`를 사용합니다.
- 역직렬화 실패는 Spring의 기본 400 응답을 사용합니다.
  - Bean Validation 실패 (`MethodArgumentNotValidException`)만 Advice에서 필드 목록으로 응답합니다.
- Jackson 3는 `FAIL_ON_NULL_FOR_PRIMITIVES`가 기본적으로 활성화되어 있습니다.
  - Jackson 2 또는 이 옵션을 비활성화한 환경에 결과를 일반화하면 안 됩니다.
  - [Jackson 3 변경 내역](https://github.com/FasterXML/jackson/wiki/Jackson-Release-3.0).
- 생성자 기본값, 빈 문자열, 나이 범위는 이 비교의 대상이 아닙니다.

## 중첩 DTO

- 모든 경로는 `POST /api/null-safety/nested` 아래에 있습니다.
- `child nullable × child @NotNull × child @Valid × Controller @Valid`의 **16개 조합 전체**를 비교합니다.
  - 자식 내부 선언은 `NestedChild(@field:NotNull val age: Int?)`로 고정합니다.
  - 부모 DTO는 8개이며, 각각 Controller의 `@Valid` O/X로 API 2개에서 사용합니다.
- 경로의 `non-null`/`nullable`은 child 타입, `not-null`은 child의 `@field:NotNull=O`를 뜻합니다.
  - `plain`은 child의 `@NotNull=X`, child의 `@Valid=X`입니다.
  - `cascade`는 child의 `@field:Valid=O`, `/valid` 접미사는 Controller의 `@Valid=O`입니다.
- Controller의 `@Valid`는 부모 DTO 검증을 시작하고, child의 `@field:Valid`는 자식 내부로 검증을 전파합니다.

| 경로                               | child nullable | child @NotNull | child @Valid | Controller @Valid | child 누락/null | child.age 누락/null |
|------------------------------------|----------------|----------------|--------------|-------------------|-----------------|---------------------|
| `/non-null/plain`                  | X              | X              | X            | X                 | 400, Jackson    | 200                 |
| `/non-null/plain/valid`            | X              | X              | X            | O                 | 400, Jackson    | 200                 |
| `/non-null/cascade`                | X              | X              | O            | X                 | 400, Jackson    | 200                 |
| `/non-null/cascade/valid`          | X              | X              | O            | O                 | 400, Jackson    | 400, `child.age`    |
| `/non-null/not-null`               | X              | O              | X            | X                 | 400, Jackson    | 200                 |
| `/non-null/not-null/valid`         | X              | O              | X            | O                 | 400, Jackson    | 200                 |
| `/non-null/not-null/cascade`       | X              | O              | O            | X                 | 400, Jackson    | 200                 |
| `/non-null/not-null/cascade/valid` | X              | O              | O            | O                 | 400, Jackson    | 400, `child.age`    |
| `/nullable/plain`                  | O              | X              | X            | X                 | 200             | 200                 |
| `/nullable/plain/valid`            | O              | X              | X            | O                 | 200             | 200                 |
| `/nullable/cascade`                | O              | X              | O            | X                 | 200             | 200                 |
| `/nullable/cascade/valid`          | O              | X              | O            | O                 | 200             | 400, `child.age`    |
| `/nullable/not-null`               | O              | O              | X            | X                 | 200             | 200                 |
| `/nullable/not-null/valid`         | O              | O              | X            | O                 | 400, `child`    | 200                 |
| `/nullable/not-null/cascade`       | O              | O              | O            | X                 | 200             | 200                 |
| `/nullable/not-null/cascade/valid` | O              | O              | O            | O                 | 400, `child`    | 400, `child.age`    |

- 각 API에 다음 입력 6종을 모두 적용합니다.
  - child 누락/null: `{}`, `{"child":null}`.
  - child 객체는 존재하지만 age 누락/null: `{"child":{}}`, `{"child":{"age":null}}`.
  - 정상 입력: `{"child":{"age":0}}`, `{"child":{"age":25}}`.
- `child.age=0` 또는 `child.age=25`는 16개 조합 모두 200 OK로 입력한 DTO를 반환합니다.
- child 자체의 null과 내부 age의 null은 별도로 차단됩니다.
  - child 자체는 non-null 타입이면 Jackson이 거부하고, nullable이면 child의 `@NotNull=O`와 Controller의 `@Valid=O`가 모두 필요합니다.
  - 내부 age는 Controller의 `@Valid=O`와 child의 `@Valid=O`가 모두 필요합니다.
  - 따라서 child에 `@Valid`를 붙여도 Controller에 `@Valid`가 없으면 내부 제약이 실행되지 않습니다.
  - `@Valid` 자체는 null 객체를 거부하지 않습니다.
- Bean Validation 실패 응답은 `{"fields":["child"]}` 또는 `{"fields":["child.age"]}`입니다.

## Map

- `Map nullable × Map @NotNull × 값 nullable × 값 @NotNull × 값 @Valid × Controller @Valid`의 **64개 조합 전체**를 비교합니다.
  - Map 키는 `String`, 값 DTO 내부는 `MapChild(@field:NotNull val age: Int?)`로 고정합니다.
  - Map 조건 4개 하위 패키지 × 값/Controller 조건 16개 API로 분리합니다.
  - 패키지마다 Controller 1개, 요청 DTO 8개를 담은 파일 1개, ApiTest 1개를 둡니다.
  - 값의 `@NotNull`과 `@Valid`는 Map 필드가 아니라 값 타입 인자에 선언합니다.

### Map 조건별 패키지와 경로 접두사

- 모든 경로는 `POST /api/null-safety/maps` 아래에 있습니다.
- 먼저 아래 접두사 하나를 선택하고, 다음 표의 공통 접미사 하나를 이어 붙입니다.
- Map 누락/null은 `{}`, `{"children":null}` 입력이며, 값/내부 age 검증과 별개입니다.

| 하위 패키지               | 경로 접두사          | Map nullable | Map @NotNull | Map 누락/null: Controller @Valid=X | Map 누락/null: Controller @Valid=O |
|---------------------------|----------------------|--------------|--------------|------------------------------------|------------------------------------|
| `map.nonnull`             | `/non-null/plain`    | X            | X            | 400, Jackson                       | 400, Jackson                       |
| `map.nonnullconstrained`  | `/non-null/not-null` | X            | O            | 400, Jackson                       | 400, Jackson                       |
| `map.nullable`            | `/nullable/plain`    | O            | X            | 200                                | 200                                |
| `map.nullableconstrained` | `/nullable/not-null` | O            | O            | 200                                | 400, `children`                    |

### 공통 값 조건 16개

- 각 접두사에 아래 16개 접미사가 모두 존재하여 총 64개 API가 됩니다.
- `plain`은 값의 `@NotNull=X`, 값의 `@Valid=X`입니다.
  - `not-null`은 값 타입 인자의 `@NotNull=O`, `cascade`는 값 타입 인자의 `@Valid=O`입니다.
  - `/valid` 접미사는 Controller 파라미터의 `@Valid=O`입니다.
- 이 표의 입력에는 Map과 `first` 키가 존재합니다.
  - null 값: `{"children":{"first":null}}`.
  - 내부 age 누락/null: `{"children":{"first":{}}}`, `{"children":{"first":{"age":null}}}`.

| 경로 접미사                               | 값 nullable | 값 @NotNull | 값 @Valid | Controller @Valid | null 값                | 내부 age 누락/null         |
|-------------------------------------------|-------------|-------------|-----------|-------------------|------------------------|----------------------------|
| `/values/non-null/plain`                  | X           | X           | X         | X                 | 400, Jackson           | 200                        |
| `/values/non-null/plain/valid`            | X           | X           | X         | O                 | 400, Jackson           | 200                        |
| `/values/non-null/cascade`                | X           | X           | O         | X                 | 400, Jackson           | 200                        |
| `/values/non-null/cascade/valid`          | X           | X           | O         | O                 | 400, Jackson           | 400, `children[first].age` |
| `/values/non-null/not-null`               | X           | O           | X         | X                 | 400, Jackson           | 200                        |
| `/values/non-null/not-null/valid`         | X           | O           | X         | O                 | 400, Jackson           | 200                        |
| `/values/non-null/not-null/cascade`       | X           | O           | O         | X                 | 400, Jackson           | 200                        |
| `/values/non-null/not-null/cascade/valid` | X           | O           | O         | O                 | 400, Jackson           | 400, `children[first].age` |
| `/values/nullable/plain`                  | O           | X           | X         | X                 | 200                    | 200                        |
| `/values/nullable/plain/valid`            | O           | X           | X         | O                 | 200                    | 200                        |
| `/values/nullable/cascade`                | O           | X           | O         | X                 | 200                    | 200                        |
| `/values/nullable/cascade/valid`          | O           | X           | O         | O                 | 200                    | 400, `children[first].age` |
| `/values/nullable/not-null`               | O           | O           | X         | X                 | 200                    | 200                        |
| `/values/nullable/not-null/valid`         | O           | O           | X         | O                 | 400, `children[first]` | 200                        |
| `/values/nullable/not-null/cascade`       | O           | O           | O         | X                 | 200                    | 200                        |
| `/values/nullable/not-null/cascade/valid` | O           | O           | O         | O                 | 400, `children[first]` | 400, `children[first].age` |

- 전체 경로 예시: `/api/null-safety/maps/nullable/not-null/values/nullable/not-null/cascade/valid`.
  - Map nullable=O, Map @NotNull=O, 값 nullable=O, 값 @NotNull=O, 값 @Valid=O, Controller @Valid=O입니다.
- Map의 `@NotNull`은 Map 내부 검증을 시작하지 않습니다. 값 내부 검증은 값의 `@Valid=O`와 Controller의 `@Valid=O`가 모두 필요합니다.
- cascade는 null 값을 건너뛰므로 값 자체 null을 막으려면 별도의 값 `@NotNull` 제약과 Controller의 `@Valid`가 필요합니다.
  - non-null 값 타입은 이와 무관하게 Jackson이 null을 거부합니다.
- 빈 Map `{"children":{}}`와 age=0, age=25인 정상 값들은 64개 조합 모두 200 OK입니다.
- 여러 값에 오류가 있으면 `children[first].age`, `children[second].age`처럼 키를 포함한 오류 경로를 정렬하여 반환합니다.
- null 값과 잘못된 내부 age를 섞은 입력도 비교합니다.
  - 입력: `{"children":{"first":null,"second":{"age":null}}}`.
  - nullable 값 + 값 `@NotNull=O` + cascade O + Controller `@Valid=O`는
    `{"fields":["children[first]","children[second].age"]}`로 응답합니다.
  - 값 타입이 non-null이면 Jackson 역직렬화가 먼저 실패하므로 Bean Validation 오류 목록까지 도달하지 않습니다.

### 타입 인자 어노테이션

```kotlin
// map.nullableconstrained 패키지의 요청 DTO
data class NullableNotNullCascadeValueRequest(
    @field:NotNull val children: Map<String, @NotNull @Valid MapChild?>?,
)
```

- Map 필드 자체의 `@field:Valid` 대신 값 타입 인자의 `@Valid`를 사용합니다.
  - 현재 Hibernate Validator는 컨테이너 필드 자체의 cascade 선언에 deprecated 경고를 출력합니다.
- 타입 인자의 Java 어노테이션이 런타임에 보이도록 모듈 컴파일 옵션에 `-Xemit-jvm-type-annotations`를 추가했습니다.
  - `-Xjsr305=strict`만으로는 충분하지 않습니다.
  - 이 설정 없이 타입 인자 제약을 선언하면 이 환경에서는 Map 값의 null 및 내부 age 검증이 실행되지 않았습니다.

## Primitive null 옵션 비교

- 기존 `scalar` 패키지의 Controller와 DTO를 그대로 사용합니다. 비교용 Controller·DTO를 추가하지 않습니다.
- 경로: `POST /api/null-safety/ages` 아래의 동일한 8개 API입니다.
- 애플리케이션을 두 모드로 실행하여 `FAIL_ON_NULL_FOR_PRIMITIVES` 옵션만 다르게 적용합니다.
  - 기본 실행은 현재 Jackson 3 기본값인 `true`를 유지합니다.
  - `primitive-defaulting` 프로필을 활성화할 때만 `false`를 설정합니다.
  - 프로필 파일: `src/main/resources/application-primitive-defaulting.yaml`.
- 이 설정은 패키지 단위가 아니라 **애플리케이션의 Jackson 설정 전체**에 적용됩니다.
  - 앞의 scalar/nested/Map 표는 기본 실행 모드 기준입니다.
  - 기존 nested/Map 예제의 내부 age는 `Int?`이므로 해당 구조의 예제를 복제하지 않습니다.

| 기존 scalar 경로           | true: 누락/null           | false: 누락/null          |
|----------------------------|---------------------------|---------------------------|
| `/non-null/plain`          | 400, Jackson              | 200, `{"age":0}`          |
| `/non-null/plain/valid`    | 400, Jackson              | 200, `{"age":0}`          |
| `/non-null/not-null`       | 400, Jackson              | 200, `{"age":0}`          |
| `/non-null/not-null/valid` | 400, Jackson              | 200, `{"age":0}`          |
| `/nullable/plain`          | 200, `{"age":null}`       | 200, `{"age":null}`       |
| `/nullable/plain/valid`    | 200, `{"age":null}`       | 200, `{"age":null}`       |
| `/nullable/not-null`       | 200, `{"age":null}`       | 200, `{"age":null}`       |
| `/nullable/not-null/valid` | 400, `{"fields":["age"]}` | 400, `{"fields":["age"]}` |

- 누락 입력은 `{}`, 명시적 null 입력은 `{"age":null}`입니다.
- 명시적 `{"age":0}`과 `{"age":25}`는 8개 API 모두 두 모드에서 200 OK로 입력값을 반환합니다.
- `false`는 `Int` 필드에 null을 저장하는 것이 아니라 **역직렬화 중 0으로 변환**합니다.
  - Bean Validation이 실행될 때는 이미 null이 아니므로 `@NotNull`로 차단할 수 없습니다.
  - 실제로 전달한 0과 누락/null에서 변환된 0을 DTO만으로 구분할 수 없습니다.
- `Int?` 입력은 null을 보존하므로 `@NotNull + @Valid`로 두 모드 모두 차단합니다.
- 이 옵션은 JVM primitive의 null 처리를 비교합니다. non-null 객체·Map 자체의 null 허용 설정이 아닙니다.

### 두 모드 실행

- 두 모드 모두 기본 포트 **8080**을 사용합니다. 한 모드를 실행하고 HTTP 파일을 보낸 뒤, 서버를 종료하고 다른 모드로 재시작합니다.

```bash
# 기본 모드: FAIL_ON_NULL_FOR_PRIMITIVES=true
./gradlew :kotlin-null-safety:bootRun

# 위 서버를 종료한 뒤 별도 프로필로 재시작: FAIL_ON_NULL_FOR_PRIMITIVES=false
./gradlew :kotlin-null-safety:bootRun --args='--spring.profiles.active=primitive-defaulting'
```

- 기존 `scalar/NullSafetyApiTest.kt`가 기본 true 모드를 검증합니다.
- 추가한 `scalar/NullSafetyDefaultingApiTest.kt`가 `@ActiveProfiles("primitive-defaulting")`로 false 모드를 검증합니다.
  - 두 테스트는 동일한 8개 API에 누락/null/0/25를 보내 각각 32건을 검증합니다.
  - 서로 다른 Spring 테스트 컨텍스트를 사용하므로 기존 기본 모드 테스트와 설정을 공유하지 않습니다.
- 추가 HTTP 파일은 `http://localhost:8080/api/null-safety/ages`의 같은 8개 API를 호출합니다.
  - 파일의 `@body`를 누락/null/0/25로 변경하며 각 모드에서 동일한 요청 8개를 실행하면 결과를 비교할 수 있습니다.
  - IDE HTTP Client에서 실행하거나, Node.js 환경에서 아래 명령으로 파일 자체를 실행합니다.

```bash
npx --yes httpyac send http/kotlin-null-safety-primitive-null.http --all --output short --no-color
```

## 파일 구성

- 기준 패키지: `me.bossm0n5t3r.nullsafety`. 애플리케이션 클래스는 루트에 유지합니다.
- `scalar/`: 단일 필드 예제
  - `NullSafetyController.kt`: 8개 직접 바인딩 API
  - `NullSafetyDto.kt`: 4개 요청 DTO와 응답 DTO
- `nested/`: 중첩 DTO 예제
  - `NestedNullSafetyController.kt`, `NestedNullSafetyDto.kt`: API 16개와 부모 DTO 8개, 자식 DTO
- `map/`: Map 예제. 공통 값 DTO는 `MapChild.kt`입니다.
  - `nonnull/`: `NonNullMapController.kt`, `NonNullMapDto.kt`
  - `nonnullconstrained/`: `NonNullConstrainedMapController.kt`, `NonNullConstrainedMapDto.kt`
  - `nullable/`: `NullableMapController.kt`, `NullableMapDto.kt`
  - `nullableconstrained/`: `NullableConstrainedMapController.kt`, `NullableConstrainedMapDto.kt`
  - 각 하위 패키지에는 API 16개, 요청 DTO 8개가 있습니다.
- `validation/`: 예제 간 공유하는 검증 오류 처리
  - `ValidationExceptionHandler.kt`: Bean Validation 오류 응답 Advice
  - `ValidationErrorResponse.kt`: 공통 오류 응답 DTO
- 테스트도 `src/test/kotlin` 아래에 동일한 `scalar/`, `nested/`, `map/` 패키지로 구분합니다.

## 테스트

- 일곱 ApiTest 모두 임의 포트의 실제 Spring Boot 서버에 HTTP 요청을 보냅니다.
  - `scalar/NullSafetyApiTest.kt`: 단일 필드 8개 조합 × 누락/null/0/25 = 32개.
  - `nested/NestedNullSafetyApiTest.kt`: 중첩 DTO 전체 16개 조합 × 입력 6종 = 96개.
  - `map/nonnull/NonNullMapApiTest.kt`: 16개 조합 × 입력 9종 = 144개.
  - `map/nonnullconstrained/NonNullConstrainedMapApiTest.kt`: 16개 조합 × 입력 9종 = 144개.
  - `map/nullable/NullableMapApiTest.kt`: 16개 조합 × 입력 9종 = 144개.
  - `map/nullableconstrained/NullableConstrainedMapApiTest.kt`: 16개 조합 × 입력 9종 = 144개.
  - `scalar/NullSafetyDefaultingApiTest.kt`: false 프로필 모드, 기존 scalar API 8개 × 입력 4종 = 32개.
- Map의 입력 9종은 Map 누락, Map null, 빈 Map, null 값, 내부 age 누락, 내부 age null, 정상 값 여러 개, 내부 오류 여러 개, null 값과 내부 오류 혼합입니다.
- Map 64개 조합의 테스트는 576건이며, 단일 필드의 두 모드와 중첩 DTO를 포함한 총 736건에서 HTTP 상태와 응답값 및 Bean Validation 오류 필드를 검증합니다.
