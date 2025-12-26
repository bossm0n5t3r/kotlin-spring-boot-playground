# Content Negotiation & Compression Experiment

이 모듈은 API 응답 포맷(직렬화)과 HTTP 압축(Content-Encoding)의 효율을 비교 실험하기 위해 구성되었습니다.

## 실행 방법

```bash
./gradlew :content-negotiation:bootRun
```

## 실험용 엔드포인트

- `GET /payload/small`: 짧은 문자열 + 소량 필드 (압축 효과 미미)
- `GET /payload/medium`: 반복되는 문자열/리스트 (압축 효율 높음)
- `GET /payload/large`: 큰 리스트 + 반복/비반복 혼합 (실제 서비스와 유사한 시나리오)

## 검증 방법 (curl)

### 1. JSON
- **무압축**: `curl -H "Accept: application/json" -H "Accept-Encoding: identity" http://localhost:8080/payload/large -D - -o /dev/null -w "Size: %{size_download} bytes\n"`
- **Gzip**: `curl -H "Accept: application/json" -H "Accept-Encoding: gzip" --compressed http://localhost:8080/payload/large -D - -o /dev/null -w "Size: %{size_download} bytes\n"`

### 2. MessagePack
- **무압축**: `curl -H "Accept: application/x-msgpack" -H "Accept-Encoding: identity" http://localhost:8080/payload/large -D - -o /dev/null -w "Size: %{size_download} bytes\n"`
- **Gzip**: `curl -H "Accept: application/x-msgpack" -H "Accept-Encoding: gzip" --compressed http://localhost:8080/payload/large -D - -o /dev/null -w "Size: %{size_download} bytes\n"`

### 3. Protobuf
- **무압축**: `curl -H "Accept: application/x-protobuf" -H "Accept-Encoding: identity" http://localhost:8080/payload/large -D - -o /dev/null -w "Size: %{size_download} bytes\n"`
- **Gzip**: `curl -H "Accept: application/x-protobuf" -H "Accept-Encoding: gzip" --compressed http://localhost:8080/payload/large -D - -o /dev/null -w "Size: %{size_download} bytes\n"`

### 결과 확인 포인트
- 응답 헤더에 `Content-Type`(json/msgpack/protobuf) 및 `Content-Encoding: gzip` 포함 여부 확인 (`-D -`)
- `Vary: Accept, Accept-Encoding` 헤더 확인
- `Size`를 통해 포맷별/압축별 전송 바이트 차이 비교

## Content Negotiation 관점 요약

- **Accept (Format)**: 클라이언트가 원하는 데이터 표현 방식(JSON, MessagePack, Protobuf 등)을 서버에 알리면, 서버는 요청에 맞는 직렬화기를 선택해 응답합니다.
- **Accept-Encoding (Compression)**: 클라이언트가 지원하는 압축 알고리즘(Gzip 등)을 명시하며, 서버는 이를 통해 응답 본문을 압축해 전송 효율을 높입니다.
- **Vary Header**: 응답이 `Accept` 및 `Accept-Encoding` 헤더에 따라 달라질 수 있음을 캐시 서버 등에 알립니다.
- 두 과정 모두 클라이언트-서버 간 최적의 통신 형태를 협상(Negotiation)하는 과정입니다.
