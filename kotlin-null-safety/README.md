# Kotlin Null Safety

- Spring MVC Controller의 `@RequestBody` 파라미터에 Kotlin DTO를 직접 선언하고, `nullable × @field:NotNull × @Valid`의 8개 조합을 비교합니다.
- 중첩 DTO의 cascade 검증도 별도 API로 비교합니다.
- String으로 받거나 수동으로 JSON을 파싱하지 않습니다.

## 실행

```bash
./gradlew :kotlin-null-safety:bootRun
./gradlew :kotlin-null-safety:test
```

- 요청 예시:
  - [단일 필드 8개 조합](../http/kotlin-null-safety.http)
  - [중첩 DTO](../http/kotlin-null-safety-nested.http)

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
- 생성자 기본값, 컬렉션, 빈 문자열, 나이 범위는 이 비교의 대상이 아닙니다.

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

## 파일 구성

- `NullSafetyController.kt`: 8개 직접 바인딩 API
- `NullSafetyDto.kt`: 4개 요청 DTO와 응답 DTO
- `NestedNullSafetyController.kt`, `NestedNullSafetyDto.kt`: 중첩 DTO 비교 API 16개와 부모 DTO 8개, 자식 DTO
- `ValidationExceptionHandler.kt`: Bean Validation 오류 응답 Advice

## 테스트

- 두 ApiTest 모두 임의 포트의 실제 Spring Boot 서버에 HTTP 요청을 보냅니다.
  - `NullSafetyApiTest`: 기존 단일 필드 8개 조합 × 누락/null/0/25 = 32개.
  - `NestedNullSafetyApiTest`: 중첩 DTO 전체 16개 조합 × 입력 6종 = 96개.
- 총 128개 시나리오에서 HTTP 상태와 허용된 입력의 응답값, Bean Validation 오류 필드를 검증합니다.
