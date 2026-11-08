# Spring Security MFA with TOTP

이 모듈은 Spring Security 7의 Multi-Factor Authentication 기능으로 **비밀번호 + TOTP(Time-based One-Time Password)** 2단계 인증을 구현한
예제입니다.

화면은 kotlinx.html로 서버에서 렌더링하고, TOTP 생성/검증은 [atlassian onetime](https://github.com/atlassian/1time), QR
코드는 [ZXing](https://github.com/zxing/zxing)을 사용합니다.

## 실행 방법

```bash
./gradlew :security-mfa-totp:bootRun
```

`http://localhost:8080`에 접속한 뒤 회원가입 → 로그인 → TOTP 설정 순서로 진행합니다. Google Authenticator 같은 인증 앱으로 QR 코드를 스캔하거나 시크릿 키를 직접
입력하면 됩니다.

> 사용자 정보는 메모리(`UserStore`)에만 저장되므로 애플리케이션을 재시작하면 사라집니다.

## 인증 흐름

```
/signup → /login ── 비밀번호 인증 (FACTOR_PASSWORD)
                     ├─ MFA 미설정 → /totp/setup  → POST /totp/setup/verify ─┐
                     └─ MFA 설정됨 → /totp/verify → POST /totp/verify ───────┤
                                                                            ├─ 성공 (FACTOR_TOTP) → /dashboard
                                                                            └─ 3회 실패 후 요청   → /login?locked
```

1. **회원가입**: `/signup` 폼으로 사용자를 생성합니다. 비밀번호는 BCrypt로 해시합니다.
2. **비밀번호 인증**: `/login`에서 로그인하면 Spring Security가 `FACTOR_PASSWORD` 권한을 부여합니다. `TotpAuthenticationHandler`가 MFA 설정 여부에
   따라 `/totp/setup` 또는 `/totp/verify`로 보냅니다.
3. **TOTP 설정 (최초 1회)**: `/totp/setup`에서 시크릿과 QR 코드를 발급하고, 인증 앱의 코드를 `/totp/setup/verify`로 제출하면 MFA가 활성화됩니다.
4. **TOTP 인증**: MFA를 설정한 사용자는 로그인할 때마다 `/totp/verify`에서 코드를 입력합니다.
5. **대시보드**: 코드가 맞으면 `FACTOR_TOTP` 권한을 추가한 인증 정보를 세션에 저장하고 `/dashboard`로 303 redirect합니다.

## 권한 구성

`SecurityConfiguration`에서 경로별로 필요한 인증 단계를 나눕니다.

| 경로                                                    | 필요한 권한        | 설명                                      |
|:--------------------------------------------------------|:-------------------|:------------------------------------------|
| `/`, `/signup`, `/login`, `/error`, `/css/**`, `/js/**` | 없음 (`permitAll`) | 공개 페이지                               |
| `/totp/**`                                              | `FACTOR_PASSWORD`  | 비밀번호 인증만 통과한 상태에서 접근 가능 |
| `/dashboard/**`                                         | `FACTOR_TOTP`      | TOTP 인증까지 완료해야 접근 가능          |
| 그 외                                                   | 인증된 사용자      |                                           |

- `@EnableMultiFactorAuthentication(authorities = [])`로 MFA 기능을 켜되, 전역 필수 factor는 두지 않고 경로별 `hasAuthority`로 제어합니다.
- `FACTOR_TOTP`는 `TotpAuthenticationHandler.grantTotpFactor`가 기존 권한에 추가합니다. `SecurityContextHolderFilter`는 컨텍스트를 자동 저장하지
  않으므로 `HttpSessionSecurityContextRepository`로 직접 저장합니다.

## 보안 처리

- **MFA 재설정 차단**: 이미 MFA를 켠 사용자가 `/totp/setup`에 접근하면 `/totp/verify`로 redirect하고, `POST /totp/setup/verify`는 403을 반환합니다.
  비밀번호만 아는 공격자가 기존 시크릿을 다시 발급받는 것을 막습니다.
- **시도 횟수 제한**: `TotpLockManager`가 사용자별 TOTP 실패 횟수를 기록합니다. 3회 실패하면 마지막 실패 시각부터 1시간 동안 잠기고, 잠긴 상태로 요청하면 세션을 무효화한 뒤
  `/login?locked`로 보냅니다. 만료 계산은 주입받은 `Clock`을 사용합니다.
- **허용 시간 범위**: `application.yaml`의 `totp.allowed.past-steps`, `future-steps`로 현재 시각 앞뒤 몇 스텝(30초 단위)의 코드를 허용할지 설정합니다.

## 주요 구성

| 파일                                  | 역할                                                          |
|:--------------------------------------|:--------------------------------------------------------------|
| `configuration/SecurityConfiguration` | 보안 필터 체인, 경로별 권한, 로그인 페이지 설정               |
| `configuration/TotpBeanConfiguration` | TOTP 생성기(SHA1, 6자리, 30초), 시크릿 생성기, QR 코드 writer |
| `totp/TotpAuthenticationHandler`      | 로그인 성공 후 TOTP 화면 분기, `FACTOR_TOTP` 부여             |
| `totp/TotpController`                 | TOTP 설정/인증 화면과 검증                                    |
| `totp/TotpService`                    | 시크릿 발급, QR 코드 생성, 코드 검증                          |
| `totp/TotpLockManager`                | TOTP 실패 횟수 기록과 잠금 판단                               |
| `user/UserStore`                      | 메모리 사용자 저장소 (`UserDetailsService`)                   |
| `web/*Controller`                     | 홈, 로그인, 회원가입, 대시보드 화면                           |
| `view/*`                              | kotlinx.html 화면                                             |
