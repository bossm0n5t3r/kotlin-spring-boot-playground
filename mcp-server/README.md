# MCP Server

Spring AI MCP Server로 Streamable HTTP transport의 MCP 서버를 구성한 예제입니다. 인증 없이 로컬에서 MCP 프로토콜 자체를 확인하는 용도입니다.

> 인증이 없으므로 `127.0.0.1`에만 바인딩합니다. 외부에 노출하지 않습니다.

## 실행 방법

```bash
./gradlew :mcp-server:bootRun
```

MCP endpoint는 `http://localhost:8087/mcp`입니다.

## Tools

데이터는 메모리에 있는 가상의 Project 3건(`project-alpha`, `project-beta`, `project-gamma`)입니다.

| Tool            | 입력          | 설명                                                |
|:----------------|:--------------|:----------------------------------------------------|
| `list_projects` | 없음          | 전체 Project 목록                                   |
| `get_project`   | `id` (string) | `id`에 해당하는 Project. 형식은 `^[a-z0-9-]{1,64}$` |

- 두 Tool 모두 `readOnlyHint=true`, `destructiveHint=false`입니다.
- 필수 인자 누락과 타입 오류는 SDK가 입력 스키마로 거부하고, 형식 오류와 없는 id는 Tool이 `isError=true`로 응답합니다.
  형식 오류 메시지에는 입력값을 넣지 않습니다.
- 등록되지 않은 Tool 호출은 JSON-RPC 오류 `-32602`를 반환합니다.

## Transport 보안

MCP SDK의 `DefaultServerTransportSecurityValidator`로 `/mcp`의 GET/POST/DELETE 요청 헤더를 검증합니다. 허용 목록은 `application.yaml`의
`playground.mcp.transport`에서 설정합니다.

| 헤더     | 허용 값                                    | 거부 시 |
|:---------|:-------------------------------------------|:--------|
| `Origin` | `http://localhost:*`, `http://127.0.0.1:*` | 403     |
| `Host`   | `localhost:*`, `127.0.0.1:*`               | 421     |

`Origin` 헤더가 없는 요청(CLI 등 브라우저가 아닌 클라이언트)은 허용합니다.

## 클라이언트 연결

### MCP Inspector

```bash
pnpx @modelcontextprotocol/inspector --cli http://localhost:8087/mcp --transport http --method tools/list
pnpx @modelcontextprotocol/inspector --cli http://localhost:8087/mcp --transport http \
  --method tools/call --tool-name get_project --tool-arg id=project-alpha
```

### Claude Code

```bash
claude mcp add --transport http playground-mcp http://localhost:8087/mcp
claude mcp list
```

## 검증 결과

| 항목         | 검증 시점 버전           |
|:-------------|:-------------------------|
| Spring Boot  | 4.2.0                    |
| Spring AI    | 2.1.0                    |
| MCP Java SDK | 2.2.0                    |
| MCP protocol | 2025-11-25 (협상된 버전) |

| Client               | initialize | tools/list | tools/call |
|:---------------------|:-----------|:-----------|:-----------|
| MCP Inspector 2.17.0 | 성공       | 성공       | 성공       |
| Claude Code 2.1.326  | 성공       | 성공       | 성공       |

- Inspector CLI는 Origin 검증 실패(403)를 `auth_required`로 표시합니다. 실제 원인은 인증이 아니라 `Invalid Origin header`입니다.
- Tool 오류 메시지는 Spring AI가 예외와 원인 예외의 메시지를 이어 붙여서 같은 문장이 두 줄로 반환됩니다.
- Codex CLI는 검증하지 않았습니다.

## 테스트

```bash
./gradlew :mcp-server:test
```
